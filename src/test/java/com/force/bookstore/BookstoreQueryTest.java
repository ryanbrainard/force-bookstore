package com.force.bookstore;

import com.force.bookstore.model.Author;
import com.force.bookstore.model.Book;
import com.force.bookstore.model.Chapter;
import com.sforce.soap.partner.sobject.SObject;
import org.junit.Before;
import org.junit.Test;

import javax.jdo.JDODetachedFieldAccessException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


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
    public void testSoqlParent1() throws Exception {
        final String soql = "SELECT Id FROM Chapter__c WHERE Id = '%s'";
        final Query query = em.createNativeQuery(String.format(soql, chapter.getId()));
        final List<SObject> resultList = query.getResultList();
        assertEquals(chapter.getId(), resultList.get(0).getId());
    }

    @Test
    public void testSoqlParent2() throws Exception {
        final String soql = "SELECT Id, Book__r.Id FROM Chapter__c WHERE Id = '%s'";
        final Query query = em.createNativeQuery(String.format(soql, chapter.getId()));
        final List<SObject> resultList = query.getResultList();
        assertEquals(book.getId(), resultList.get(0).getChild("book__r").getField("Id"));
    }

    @Test
    public void testSoqlParent3() throws Exception {
        final String soql = "SELECT Id, Book__r.Id, Book__r.Author__r.Id FROM Chapter__c WHERE Id = '%s'";
        final Query query = em.createNativeQuery(String.format(soql, chapter.getId()));
        final List<SObject> resultList = query.getResultList();
        assertEquals(author.getId(), resultList.get(0).getChild("book__r").getChild("author__r").getField("Id"));
    }

    @Test
    public void testSoqlChildren() throws Exception {
        final String soql = "SELECT Id, (SELECT Id FROM author_Books__r) FROM Author__c WHERE Id = '%s'";
        final Query query = em.createNativeQuery(String.format(soql, author.getId()));
        final List<SObject> resultList = query.getResultList();
        assertEquals(book.getId(), resultList.get(0).getChild("author_Books__r").getChild("records").getField("Id"));
    }

    @Test
    public void testJpqlParents() throws Exception {
        final String jpql = "SELECT c FROM Chapter c  WHERE c.id = '%s'";
        final TypedQuery<Chapter> query = em.createQuery(String.format(jpql, chapter.getId()), Chapter.class);
        final List<Chapter> resultList = query.getResultList();
        assertEquals(chapter.getId(), resultList.get(0).getId());
        assertEquals(book.getId(), resultList.get(0).getBook().getId());
        assertEquals(author.getId(), resultList.get(0).getBook().getAuthor().getId());
    }

    @Test
    public void testJpqlChildren_NonTransactional() throws Exception {
        final String jpql = "SELECT a FROM Author a  WHERE a.id = '%s'";
        final TypedQuery<Author> query = em.createQuery(String.format(jpql, author.getId()), Author.class);
        final List<Author> resultList = query.getResultList();
        assertEquals(author.getId(), resultList.get(0).getId());

        try {
            resultList.get(0).getBooks().iterator().next().getId();
            fail();
        } catch (JDODetachedFieldAccessException e) {
            // expected because not in a transaction
        }
    }

    @Test
    public void testFindChildren_NonTransactional() throws Exception {
        final Author foundAuthor = em.find(Author.class, author.getId());
        try {
            foundAuthor.getBooks().iterator().next().getId();
            fail();
        } catch (JDODetachedFieldAccessException e) {
            // expected because not in a transaction
        }
    }

    @Test
    public void testJpqlChildren_Transactional() throws Exception {
        final Author queriedAuthor = bookstore.queryAuthor(author.getId());
        assertEquals(author.getId(), queriedAuthor.getId());
        assertEquals(book.getId(), queriedAuthor.getBooks().iterator().next().getId());
    }

    @Test
    public void testFindChildren_Transactional() throws Exception {
        final Author foundAuthor = bookstore.findAuthor(author.getId());
        assertEquals(book.getId(), foundAuthor.getBooks().iterator().next().getId());
    }
}
