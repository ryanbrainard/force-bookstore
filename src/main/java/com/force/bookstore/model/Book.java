package com.force.bookstore.model;

import com.force.bookstore.service.Persistable;

import javax.persistence.*;

/**
 * @author rbrainard
 */
@Entity
public class Book implements Persistable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private String id;

    private String title;

    @ManyToOne
    private Author author;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Author getAuthor() {
        return author;
    }

    public void setAuthor(Author author) {
        this.author = author;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
