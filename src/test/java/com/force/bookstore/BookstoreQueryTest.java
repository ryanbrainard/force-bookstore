package com.force.bookstore;

import com.force.bookstore.model.Author;
import com.force.bookstore.model.Book;
import com.force.bookstore.model.Chapter;
import com.force.bookstore.model.Part;
import com.sforce.soap.partner.sobject.SObject;
import org.junit.Before;
import org.junit.Test;

import javax.jdo.JDODetachedFieldAccessException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;


public class BookstoreQueryTest extends PersistableBaseTest {

    private Author author;
    private Author ghostWriter;
    private Book book;
    private Part part;
    private Chapter chapter;

    @Before
    public void setUp() {
        author = register(new Author());
        author.setLastName(AUTHOR_LAST_NAME);
        bookstore.save(author);

        ghostWriter = register(new Author());
        ghostWriter.setLastName("CASPER");
        bookstore.save(ghostWriter);

        book = register(new Book());
        book.setAuthor(author);
        book.setGhostWriter(ghostWriter);
        book.setTitle(TITLE_1);
        bookstore.save(book);

        part = register(new Part());
        part.setBook(book);
        bookstore.save(part);

        chapter = register(new Chapter());
        chapter.setBook(book);
        chapter.setPart(part);
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
        assertEquals(book.getTitle(), resultList.get(0).getBook().getTitle());
        assertEquals(book.getTitle(), TITLE_1);
        assertEquals(author.getLastName(), resultList.get(0).getBook().getAuthor().getLastName());
        assertEquals(author.getLastName(), AUTHOR_LAST_NAME);
    }

    @Test
    public void testJpqlParents_TransactionalWithRefresh() throws Exception {
        final String jpql = "SELECT c FROM Chapter c  WHERE c.id = '%s'";
        final List<Chapter> resultList = bookstore.queryAndRefresh(String.format(jpql, chapter.getId()), Chapter.class);

        assertEquals(chapter.getId(), resultList.get(0).getId());
        assertEquals(book.getId(), resultList.get(0).getBook().getId());
        assertEquals(book.getTitle(), resultList.get(0).getBook().getTitle());
        assertEquals(book.getTitle(), TITLE_1);
        assertEquals(author.getLastName(), resultList.get(0).getBook().getAuthor().getLastName());
        assertEquals(author.getLastName(), AUTHOR_LAST_NAME);
    }

    @Test
    public void testJpqlMultipleParents() throws Exception {
        final String jpql = "SELECT c FROM Chapter c  WHERE c.id = '%s'";
        final TypedQuery<Chapter> query = em.createQuery(String.format(jpql, chapter.getId()), Chapter.class);
        final List<Chapter> resultList = query.getResultList();
        assertEquals(chapter.getId(), resultList.get(0).getId());
        assertEquals(book.getId(), resultList.get(0).getBook().getId());
        assertEquals(book.getTitle(), resultList.get(0).getBook().getTitle());
        assertEquals(book.getTitle(), TITLE_1);
        assertEquals(author.getLastName(), resultList.get(0).getBook().getAuthor().getLastName());
        assertEquals(author.getLastName(), AUTHOR_LAST_NAME);

        assertEquals(part.getId(), resultList.get(0).getPart().getId());
        assertEquals(part.getBook().getId(), resultList.get(0).getPart().getBook().getId());
        assertEquals(part.getBook().getAuthor().getId(), resultList.get(0).getPart().getBook().getAuthor().getId());

        /*

         Failing because part__r.book__r.author__r is never loaded

         select id,
         book__r.author__r.authorUniversalId__c,
         book__r.author__r.birthDate__c,
         book__r.author__r.firstName__c,
         book__r.author__r.Id,
         book__r.author__r.lastName__c,
         book__r.Id,
         book__r.title__c,
         part__r.book__r.Id,
         part__r.book__r.title__c,
         part__r.Id from Chapter__c c  where (c.Id = 'a03U0000001GKXoIAO')
          */
    }

    @Test
    public void testJpqlMultipleParents_TransactionalWithRefresh() throws Exception {
        final String jpql = "SELECT c FROM Chapter c  WHERE c.id = '%s'";
        final List<Chapter> resultList = bookstore.queryAndRefresh(String.format(jpql, chapter.getId()), Chapter.class);
        assertEquals(chapter.getId(), resultList.get(0).getId());
        assertEquals(book.getId(), resultList.get(0).getBook().getId());
        assertEquals(book.getTitle(), resultList.get(0).getBook().getTitle());
        assertEquals(book.getTitle(), TITLE_1);
        assertEquals(author.getLastName(), resultList.get(0).getBook().getAuthor().getLastName());
        assertEquals(author.getLastName(), AUTHOR_LAST_NAME);

        assertEquals(part.getId(), resultList.get(0).getPart().getId());
        assertEquals(part.getBook().getId(), resultList.get(0).getPart().getBook().getId());
        assertEquals(part.getBook().getAuthor().getId(), resultList.get(0).getPart().getBook().getAuthor().getId());
    }

    @Test
    public void testFindParents_TransactionalWithRefresh() throws Exception {
        final List<Chapter> resultList = Arrays.asList(bookstore.findAndRefresh(chapter.getId(), Chapter.class));

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
    public void testJpqlMultipleParentsOfSameType() throws Exception {
        final String jpql = "SELECT b FROM Book b  WHERE b.id = '%s'";
        final Book queriedBook = bookstore.queryAndRefresh(String.format(jpql, book.getId()), Book.class).get(0);

        assertEquals(book.getId(), book.getId());

        assertEquals(author.getId(), book.getAuthor().getId());
        assertEquals(author.getLastName(), book.getAuthor().getLastName());

        assertEquals(ghostWriter.getId(), book.getGhostWriter().getId());
        assertEquals(ghostWriter.getLastName(), book.getGhostWriter().getLastName());
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
