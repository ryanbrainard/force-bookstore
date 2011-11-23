Summary
=======

If an object has a lookup relationship (many to one) to another object, the `@PostLoad` annotated methods on the parent
object are not called when the child object is loaded. For example, if `Book` looks up to `Author` and `Author` has a
`postLoad()` method annotated with `@PostLoad`, and `em.find(Book.class, book.getId())` is called, `Author.postLoad()`
is never called even though the `Author` object is loaded implictly and assigned to `Book.author`. However, loading
the `Author` directly with `em.find(Author.class, author.getId())` does call `Author.postLoad()`.

Below are tests that demonstrate this behavior:

    // PASSING
    @Test
    public void testAuthorPostLoadHook_DirectlyLoadAuthor() throws Exception {
        final Author author = register(new Author());
        bookstore.save(author);

        final Book book = register(new Book());
        book.setAuthor(author);
        bookstore.save(book);

        final Author directlyLoadedAuthor = em.find(Author.class, author.getId());
        assertTrue(directlyLoadedAuthor.wasPostLoadHookCalled());
    }

    // FAILING
    @Test
    public void testAuthorPostLoadHook_IndirectlyLoadAuthorViaBook() throws Exception {
        final Author author = register(new Author());
        bookstore.save(author);

        final Book book = register(new Book());
        book.setAuthor(author);
        bookstore.save(book);

        final Book directlyLoadedBook = em.find(Book.class, book.getId());
        assertTrue(directlyLoadedBook.wasPostLoadHookCalled());
        assertTrue(directlyLoadedBook.getAuthor().wasPostLoadHookCalled());
    }

These same tests were run with DataNucleus+HSQLDB and Hibernate+HSQLDB as points of comparison, and both tests
passed in both cases, which shows that it is an issue solely with the Force.com SDK; however, it appears to be happening
because SOQL allows for both objects to be queried at once, whereas the others make multiple queries to get the data
and populate the objects.

Force SDK
---------

1. One SOQL query is run that fetches both `Book` and `Author`: `select id, author__r.authorUniversalId__c, author__r.birthDate__c, author__r.firstName__c, author__r.Id, author__r.lastName__c, title__c from Book__c where Id='a01U0000001VFO3IAO'`
2. Book constructed
3. Author constructed
4. Book.author set
5. Book.postLoad() called

Callstack when `author` is set on `Book` using `replaceFields()`:

        at com.force.bookstore.model.Book.jdoReplaceField(Book.java:-1)
        at com.force.bookstore.model.Book.jdoReplaceFields(Book.java:-1)
        at org.datanucleus.jdo.state.JDOStateManagerImpl.replaceFields(JDOStateManagerImpl.java:2983)
        at org.datanucleus.jdo.state.JDOStateManagerImpl.replaceFields(JDOStateManagerImpl.java:3003)
        at org.datanucleus.state.ObjectProviderImpl.replaceFields(ObjectProviderImpl.java:70)
        at com.force.sdk.jpa.ForcePersistenceHandler.fetchObject(ForcePersistenceHandler.java:136)
        at org.datanucleus.jdo.state.JDOStateManagerImpl.loadFieldsFromDatastore(JDOStateManagerImpl.java:2028)
        at org.datanucleus.jdo.state.JDOStateManagerImpl.validate(JDOStateManagerImpl.java:4528)
        at org.datanucleus.ObjectManagerImpl.findObject(ObjectManagerImpl.java:2809)
        at org.datanucleus.jpa.EntityManagerImpl.find(EntityManagerImpl.java:305)
        at com.force.sdk.jpa.ForceEntityManager.find(ForceEntityManager.java:294)
        at org.datanucleus.jpa.EntityManagerImpl.find(EntityManagerImpl.java:236)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java:-1)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
        at java.lang.reflect.Method.invoke(Method.java:597)
        at org.springframework.orm.jpa.ExtendedEntityManagerCreator$ExtendedEntityManagerInvocationHandler.invoke(ExtendedEntityManagerCreator.java:365)
        at $Proxy32.find(Unknown Source:-1)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java:-1)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
        at java.lang.reflect.Method.invoke(Method.java:597)
        at org.springframework.orm.jpa.SharedEntityManagerCreator$SharedEntityManagerInvocationHandler.invoke(SharedEntityManagerCreator.java:240)
        at $Proxy32.find(Unknown Source:-1)
        at com.force.bookstore.BookstoreCrudTest.txFind(BookstoreCrudTest.java:186)
        at com.force.bookstore.BookstoreCrudTest.testAuthorPostLoadHook_IndirectlyLoadAuthorViaBook(BookstoreCrudTest.java:112)

DataNucleus + HSQLDB
--------------------

1. Book constructed
2. Book is queried: `SELECT A0.AUTHOR_ID,A0.TITLE FROM BOOK A0 WHERE A0.ID`
3. Author constructed
4. Book.author set
5. Book.postLoad() called
6. Author is queried: `SELECT A0.AUTHORUNIVERSALID,A0.BIRTHDATE,A0.FIRSTNAME,A0.LASTNAME FROM AUTHOR A0 WHERE A0.ID = <0>`
7. Author.postLoad() called

Callstack when `Book.postLoad()` is called:

	  at com.force.bookstore.model.Book.postLoad(Book.java:60)
	  at sun.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java:-1)
	  at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	  at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	  at java.lang.reflect.Method.invoke(Method.java:597)
	  at org.datanucleus.jpa.JPACallbackHandler$1.run(JPACallbackHandler.java:406)
	  at java.security.AccessController.doPrivileged(AccessController.java:-1)
	  at org.datanucleus.jpa.JPACallbackHandler.invokeCallbackMethod(JPACallbackHandler.java:394)
	  at org.datanucleus.jpa.JPACallbackHandler.invokeCallback(JPACallbackHandler.java:347)
	  at org.datanucleus.jpa.JPACallbackHandler.postLoad(JPACallbackHandler.java:181)
	  at org.datanucleus.jdo.state.JDOStateManagerImpl.postLoad(JDOStateManagerImpl.java:4684)
	  at org.datanucleus.jdo.state.JDOStateManagerImpl.validate(JDOStateManagerImpl.java:4531)
	  at org.datanucleus.ObjectManagerImpl.findObject(ObjectManagerImpl.java:2809)
	  at org.datanucleus.jpa.EntityManagerImpl.find(EntityManagerImpl.java:305)
	  at com.force.sdk.jpa.ForceEntityManager.find(ForceEntityManager.java:294)
	  at org.datanucleus.jpa.EntityManagerImpl.find(EntityManagerImpl.java:236)
	  at sun.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java:-1)
	  at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	  at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	  at java.lang.reflect.Method.invoke(Method.java:597)
	  at org.springframework.orm.jpa.ExtendedEntityManagerCreator$ExtendedEntityManagerInvocationHandler.invoke(ExtendedEntityManagerCreator.java:365)
	  at $Proxy30.find(Unknown Source:-1)
	  at sun.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java:-1)
	  at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	  at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	  at java.lang.reflect.Method.invoke(Method.java:597)
	  at org.springframework.orm.jpa.SharedEntityManagerCreator$SharedEntityManagerInvocationHandler.invoke(SharedEntityManagerCreator.java:240)
	  at $Proxy30.find(Unknown Source:-1)
	  at com.force.bookstore.BookstoreCrudTest.txFind(BookstoreCrudTest.java:186)
	  at com.force.bookstore.BookstoreCrudTest.testAuthorPostLoadHook_IndirectlyLoadAuthorViaBook(BookstoreCrudTest.java:112)


Callstack when `Author.postLoad()` is called:

	  at com.force.bookstore.model.Author.postLoad(Author.java:90)
	  at sun.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java:-1)
	  at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	  at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	  at java.lang.reflect.Method.invoke(Method.java:597)
	  at org.datanucleus.jpa.JPACallbackHandler$1.run(JPACallbackHandler.java:406)
	  at java.security.AccessController.doPrivileged(AccessController.java:-1)
	  at org.datanucleus.jpa.JPACallbackHandler.invokeCallbackMethod(JPACallbackHandler.java:394)
	  at org.datanucleus.jpa.JPACallbackHandler.invokeCallback(JPACallbackHandler.java:347)
	  at org.datanucleus.jpa.JPACallbackHandler.postLoad(JPACallbackHandler.java:181)
	  at org.datanucleus.jdo.state.JDOStateManagerImpl.postLoad(JDOStateManagerImpl.java:4684)
	  at org.datanucleus.jdo.state.JDOStateManagerImpl.loadUnloadedFieldsInFetchPlan(JDOStateManagerImpl.java:1764)
	  at org.datanucleus.jdo.state.JDOStateManagerImpl.detach(JDOStateManagerImpl.java:3786)
	  at org.datanucleus.ObjectManagerImpl.detachObject(ObjectManagerImpl.java:1983)
	  at com.force.sdk.jpa.ForceObjectManagerImpl.detachObject(ForceObjectManagerImpl.java:225)
	  at org.datanucleus.ExecutionContextImpl.detachObject(ExecutionContextImpl.java:257)
	  at org.datanucleus.store.fieldmanager.DetachFieldManager.processPersistable(DetachFieldManager.java:92)
	  at org.datanucleus.store.fieldmanager.DetachFieldManager.internalFetchObjectField(DetachFieldManager.java:129)
	  at org.datanucleus.store.fieldmanager.AbstractFetchFieldManager.fetchObjectField(AbstractFetchFieldManager.java:103)
	  at org.datanucleus.jdo.state.JDOStateManagerImpl.detach(JDOStateManagerImpl.java:3813)
	  at org.datanucleus.ObjectManagerImpl.performDetachOnClose(ObjectManagerImpl.java:3643)
	  at org.datanucleus.ObjectManagerImpl.close(ObjectManagerImpl.java:891)
	  at org.datanucleus.jdo.JDOPersistenceManager.internalClose(JDOPersistenceManager.java:283)
	  at org.datanucleus.jdo.JDOPersistenceManagerFactory.releasePersistenceManager(JDOPersistenceManagerFactory.java:1018)
	  at org.datanucleus.jdo.JDOPersistenceManager.close(JDOPersistenceManager.java:267)
	  at org.datanucleus.jpa.EntityManagerImpl.close(EntityManagerImpl.java:173)
	  at com.force.sdk.jpa.ForceEntityManager.close(ForceEntityManager.java:106)
	  at sun.reflect.NativeMethodAccessorImpl.invoke0(NativeMethodAccessorImpl.java:-1)
	  at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:39)
	  at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:25)
	  at java.lang.reflect.Method.invoke(Method.java:597)
	  at org.springframework.orm.jpa.ExtendedEntityManagerCreator$ExtendedEntityManagerInvocationHandler.invoke(ExtendedEntityManagerCreator.java:365)
	  at $Proxy30.close(Unknown Source:-1)
	  at org.springframework.orm.jpa.EntityManagerFactoryUtils.closeEntityManager(EntityManagerFactoryUtils.java:331)
	  at org.springframework.orm.jpa.SharedEntityManagerCreator$SharedEntityManagerInvocationHandler.invoke(SharedEntityManagerCreator.java:260)
	  at $Proxy30.find(Unknown Source:-1)
	  at com.force.bookstore.BookstoreCrudTest.txFind(BookstoreCrudTest.java:186)
	  at com.force.bookstore.BookstoreCrudTest.testAuthorPostLoadHook_IndirectlyLoadAuthorViaBook(BookstoreCrudTest.java:112)



Hibernate + HSQLDB
------------------
