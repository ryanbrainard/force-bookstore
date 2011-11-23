package com.force.bookstore.model;

import com.force.bookstore.service.Persistable;
import com.force.sdk.jpa.annotation.CustomField;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

/**
 * @author rbrainard
 */
@Entity
public class Author implements Persistable {

    @Transient private boolean postLoadHookCalled;
    @Transient private boolean prePersistCalled;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
	private String id;

    @CustomField(externalId = true)
    private String authorUniversalId;

    private String firstName;
    private String lastName;
    private Date birthDate;

    @OneToMany(mappedBy = "author")
    private Set<Book> books;

    public Author() {
    }

    public Set<Book> getBooks() {
        return books;
    }

    public void setBooks(Set<Book> books) {
        this.books = books;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(Date birthDate) {
        this.birthDate = birthDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorUniversalId() {
        return authorUniversalId;
    }

    public void setAuthorUniversalId(String authorUniversalId) {
        this.authorUniversalId = authorUniversalId;
    }

    @PrePersist
    public void prePersist() {
        prePersistCalled = true;
    }

    @PostLoad
    public void postLoad() {
        postLoadHookCalled = true;
        System.out.println("POST LOAD CALLBACK ON AUTHOR");
    }

    public boolean wasPrePersistHookCalled() {
        return prePersistCalled;
    }

    public boolean wasPostLoadHookCalled() {
        return postLoadHookCalled;
    }
}
