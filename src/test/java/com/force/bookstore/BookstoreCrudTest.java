package com.force.bookstore;

import com.force.bookstore.model.Author;
import com.force.bookstore.model.Book;
import com.force.bookstore.service.BookstoreService;
import com.force.bookstore.service.Persistable;
import com.force.sdk.connector.ForceServiceConnector;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/app-context.xml")
public class BookstoreCrudTest {

    @Inject private BookstoreService bookstore;
    @Inject private ForceServiceConnector connector;
    private Set<Persistable> createdEntities = new HashSet<Persistable>();


    @Test
    public void testEntityCRUD() throws Exception {
        final String authorFirstName = "John";
        final String authorLastName = "Steinbeck";
        final String title1 = "Of Mice and Men";
        final String title2 = "The Grapes of Wrath";

        final Author author = register(new Author());
        author.setFirstName(authorFirstName);
        author.setLastName(authorLastName);
        bookstore.save(author);
        assertNotNull(author.getId());
//        assertEquals(title1,  bookstore.findBook(author.getId()).getTitle());

        final Book book1 = register(new Book());
        book1.setAuthor(author);
        book1.setTitle(title1);
        bookstore.save(book1);
        assertNotNull(book1.getId());
        assertEquals(title1,  bookstore.findBook(book1.getId()).getTitle());

        final Book book2 = register(new Book());
        book2.setAuthor(author);
        book2.setTitle(title2);
        bookstore.save(book2);
        assertNotNull(book2.getId());
        assertEquals(title2, bookstore.findBook(book2.getId()).getTitle());
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
