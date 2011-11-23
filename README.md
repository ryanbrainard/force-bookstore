Summary
=======

If an object has a lookup relationship (many to one) to another object, the `@PostLoad` annotated methods on the parent
object are not called when the child object is loaded. For example, if `Book` looks up to `Author` and `Author` has a
`postLoad()` method annotated with `@PostLoad`, and `em.find(Book.class, book.getId())` is called, `Author.postLoad()`
is never called. However, loading the `Author` directly with `em.find(Author.class, author.getId())` does call `Author.postLoad()`.

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

, like this:

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

This is happening because the Force.com SDK loads both the `Book` and `Author` objects in one SOQL relationship query and is setting `Book.author` is just

DataNucleus + Force SDK
-----------------------


DataNucleus + HSQLDB
--------------------


Hibernate + HSQLDB
------------------
