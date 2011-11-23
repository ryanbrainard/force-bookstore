package com.force.bookstore.model;

import com.force.bookstore.service.Persistable;

import javax.persistence.*;

/**
 * @author rbrainard
 */
@Entity
public class Chapter implements Persistable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    @ManyToOne
    private Book book;

    @Override
    public String getId() {
        return id;
    }

    public void setBook(Book book) {
        this.book = book;
    }
}
