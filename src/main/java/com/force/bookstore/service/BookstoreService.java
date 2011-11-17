package com.force.bookstore.service;

import com.force.bookstore.model.Author;
import com.force.bookstore.model.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Service
public class BookstoreService {

    private static final Logger logger = LoggerFactory.getLogger(BookstoreService.class);

    @PersistenceContext
    EntityManager em;

    public void setEntityManager(EntityManager em) {
        this.em = em;
    }

    @Transactional(readOnly = true)
    public Book findBook(String id) {
        if ("new".equals(id)) {
            return new Book();
        } else {
            return em.find(Book.class, id);
        }
    }

    @Transactional
    public Book save(Book book) {
        if (book.getId() != null) {
            book = em.merge(book);
        } else {
            em.persist(book);
        }
        logger.info("book saved: " + em);

        return book;
    }

    @Transactional
    public Author save(Author author) {
        if (author.getId() != null) {
            author = em.merge(author);
        } else {
            em.persist(author);
        }
        logger.info("author saved: " + em);

        return author;
    }

    @Transactional
    public boolean deleteBook(String bookId) {
        Book book = findBook(bookId);

        if (book == null) {
            return false;
        }

        em.remove(book);
        return true;
    }

}
