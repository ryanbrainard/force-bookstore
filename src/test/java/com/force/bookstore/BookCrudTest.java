package com.force.bookstore;

import com.force.bookstore.model.Book;
import com.force.bookstore.service.BookstoreService;
import com.force.sdk.connector.ForceServiceConnector;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.inject.Inject;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/app-context.xml")
public class BookCrudTest {

    @Inject
    private BookstoreService bookstore;

    @Inject
    private ForceServiceConnector connector;

    @Test
    public void testEntityCRUD() throws Exception {
        final String title1 = "Gone with the Wind";
        final String title2 = title1 + " Part 2";

        Integer bookId = null;

        try {
            Book book = new Book();
            book.setTitle(title1);
            bookstore.save(book);
            assertNotNull(book.getId());

            bookId = book.getId();
            book = bookstore.findBook(bookId);
            assertEquals(title1, book.getTitle());

            book.setTitle(title2);
            bookstore.save(book);
            book = bookstore.findBook(bookId);
            assertEquals(title2, book.getTitle());

            bookstore.deleteBook(bookId);
            book = bookstore.findBook(bookId);
            assertNull(book);
            bookId = null;
        } finally {
//            if (bookId != null) {
//                connector.getConnection().delete(new Integer[]{bookId});
//            }
        }
    }
}
