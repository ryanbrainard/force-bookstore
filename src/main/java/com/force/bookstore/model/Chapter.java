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

    @ManyToOne
    private Part part;

    public Chapter() {
    }

    @Override
    public String getId() {
        return id;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Book getBook() {
        return book;
    }

    public Part getPart() {
        return part;
    }

    public void setPart(Part part) {
        this.part = part;
    }
}
