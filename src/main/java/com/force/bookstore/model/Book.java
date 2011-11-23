package com.force.bookstore.model;

import com.force.bookstore.service.Persistable;

import javax.persistence.*;

/**
 * @author rbrainard
 */
@Entity
public class Book implements Persistable {

    @Transient
    private boolean postLoadHookCalled;
    @Transient
    private boolean prePersistCalled;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String title;

    @ManyToOne
    private Author author;


    public Book() {
    }

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

    @PrePersist
    public void prePersist() {
        prePersistCalled = true;
    }

    @PostLoad
    public void postLoad() {
        postLoadHookCalled = true;
        System.out.println("POST LOAD CALLBACK ON BOOK");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;

        if (id != null ? !id.equals(book.id) : book.id != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", author=" + author +
                '}';
    }

    public boolean wasPrePersistHookCalled() {
        return prePersistCalled;
    }

    public boolean wasPostLoadHookCalled() {
        return postLoadHookCalled;
    }
}
