package com.force.bookstore;

import com.force.bookstore.model.Author;
import com.force.bookstore.model.Book;
import com.force.bookstore.service.BookstoreService;
import com.force.bookstore.service.Persistable;
import com.force.sdk.connector.ForceServiceConnector;
import com.google.common.collect.Sets;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import javax.jdo.annotations.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/app-context.xml")
public class BookstoreCrudTest {

    public static final String AUTHOR_FIRST_NAME = "John";
    public static final String AUTHOR_LAST_NAME = "Steinbeck";
    public static final String TITLE_1 = "Of Mice and Men";
    public static final String TITLE_2 = "The Grapes of Wrath";

    private Set<Persistable> createdEntities = new HashSet<Persistable>();

    @Inject private BookstoreService bookstore;
    @Inject private ForceServiceConnector connector;
    @PersistenceContext private EntityManager em;

    @Test
    public void testCreateAuthorWithBooks() throws Exception {
        final Author author = register(new Author());
        author.setFirstName(AUTHOR_FIRST_NAME);
        author.setLastName(AUTHOR_LAST_NAME);
        bookstore.save(author);

        final Book book1 = register(new Book());
        book1.setAuthor(author);
        book1.setTitle(TITLE_1);
        bookstore.save(book1);

        final Book book2 = register(new Book());
        book2.setAuthor(author);
        book2.setTitle(TITLE_2);
        bookstore.save(book2);

        final Author persistedAuthor = bookstore.findAuthor(author.getId());
        assertEquals(AUTHOR_FIRST_NAME, persistedAuthor.getFirstName());
        assertEquals(AUTHOR_LAST_NAME, persistedAuthor.getLastName());
        assertEquals(null, persistedAuthor.getBirthDate());
        assertEquals(Sets.newHashSet(book1, book2), persistedAuthor.getBooks());
    }

    @Test
    public void testHooksOnAuthorOnly() throws Exception {
        final Author author = register(new Author());
        author.setFirstName(AUTHOR_FIRST_NAME);
        author.setLastName(AUTHOR_LAST_NAME);

        assertFalse(author.wasPrePersistHookCalled());
        bookstore.save(author);
        assertTrue(author.wasPrePersistHookCalled());

        assertFalse(author.wasPostLoadHookCalled());
        assertTrue(bookstore.findAuthor(author.getId()).wasPostLoadHookCalled());
    }

    @Test
    public void testHooksOnBookOnly() throws Exception {
        final Book book = register(new Book());

        assertFalse(book.wasPrePersistHookCalled());
        bookstore.save(book);
        assertTrue(book.wasPrePersistHookCalled());

        assertFalse(book.wasPostLoadHookCalled());
        assertTrue(bookstore.findBook(book.getId()).wasPostLoadHookCalled());
    }

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

    @Test
    public void testBookPostLoadHook_DirectlyLoadBook() throws Exception {
        final Author author = register(new Author());
        bookstore.save(author);

        final Book book = register(new Book());
        book.setAuthor(author);
        bookstore.save(book);

        final Book directlyLoadedBook = txFind(Book.class, book.getId());
        assertTrue(directlyLoadedBook.wasPostLoadHookCalled());
    }

    @Test
    public void testBookPostLoadHook_IndirectlyLoadBookViaAuthor_WithService() throws Exception {
        final Author author = register(new Author());
        bookstore.save(author);

        final Book book = register(new Book());
        book.setAuthor(author);
        bookstore.save(book);

        final Author directlyLoadedAuthor = bookstore.findAuthor(author.getId());
        assertTrue(directlyLoadedAuthor.wasPostLoadHookCalled());
        assertTrue(directlyLoadedAuthor.getBooks().iterator().next().wasPostLoadHookCalled());
    }

    @Test
    public void testBookPostLoadHook_IndirectlyLoadBookViaAuthor_WithCallable() throws Exception {
        final Author author = register(new Author());
        bookstore.save(author);

        final Book book = register(new Book());
        book.setAuthor(author);
        bookstore.save(book);

        txCall(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final Author directlyLoadedAuthor = txFind(Author.class, author.getId());
                assertTrue(directlyLoadedAuthor.wasPostLoadHookCalled());
                assertTrue(directlyLoadedAuthor.getBooks().iterator().next().wasPostLoadHookCalled());
                return null;
            }
        });
    }

    @Test
    public void testBookPostLoadHook_IndirectlyLoadBookViaAuthor_PrivateTransactionMethod() throws Exception {
        final Author author = register(new Author());
        bookstore.save(author);

        final Book book = register(new Book());
        book.setAuthor(author);
        bookstore.save(book);

        txFindAndAssert(author);
    }

    @Transactional
    private void txFindAndAssert(Author author) {
        final Author directlyLoadedAuthor = txFind(Author.class, author.getId());
        assertTrue(directlyLoadedAuthor.wasPostLoadHookCalled());
        assertTrue(directlyLoadedAuthor.getBooks().iterator().next().wasPostLoadHookCalled());
    }


    @Transactional
    public <T> T txFind(Class<T> entityClass, Object primaryKey) {
        return em.find(entityClass, primaryKey);
    }

    @Transactional
    public <V> V txCall(Callable<V> callable) throws Exception {
        return callable.call();
    }

    @After
    public void tearDown() throws Exception {
        Set<String> ids = new HashSet<String>();
        for (Persistable p : createdEntities) {
            if (null != p.getId()) {
                ids.add(p.getId());
            }
        }

        connector.getConnection().delete(ids.toArray(new String[ids.size()]));
    }

    private <E extends Persistable> E register(E e) {
        createdEntities.add(e);
        return e;
    }
}
