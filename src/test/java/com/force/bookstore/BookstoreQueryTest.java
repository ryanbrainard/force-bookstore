package com.force.bookstore;

import com.force.bookstore.model.Author;
import com.force.bookstore.model.Book;
import com.force.bookstore.model.Chapter;
import com.sforce.soap.partner.sobject.SObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.persistence.Query;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/app-context.xml")
public class BookstoreQueryTest extends PersistableBaseTest {

    private Author author;
    private Book book;
    private Chapter chapter;

    @Before
    public void setUp() {
        author = register(new Author());
        bookstore.save(author);

        book = register(new Book());
        book.setAuthor(author);
        bookstore.save(book);

        chapter = register(new Chapter());
        chapter.setBook(book);
        bookstore.save(chapter);
    }

    @Test
    public void testOneLevelNativeQuery() throws Exception {
        final String soql = "SELECT Id FROM Chapter__c WHERE Id = '%s'";
        final Query query = em.createNativeQuery(String.format(soql, chapter.getId()));
        final List<SObject> resultList = query.getResultList();
        assertEquals(chapter.getId(), resultList.get(0).getId());
    }

}
