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
        return em.find(Book.class, id);
    }

    @Transactional
    public Author findAuthor(String id) {
        final Author author = em.find(Author.class, id);
        author.getBooks(); // calling inside tx to load
        return author;
    }

    @Transactional
    public <P extends Persistable> P save(P persistable) {
        if (persistable.getId() != null) {
            persistable = em.merge(persistable);
        } else {
            em.persist(persistable);
        }
        logger.info("saved: " + em);

        return persistable;
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
