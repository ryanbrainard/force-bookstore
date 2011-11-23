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

    @Transient private boolean posLoadHookCalled;
    @Transient private boolean prePersistCalled;

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
	private Integer id;

    @CustomField(externalId = true)
    private String authorUniversalId;

    private String firstName;
    private String lastName;
    private Date birthDate;

    public Author() {
    }

    @OneToMany(mappedBy = "author")
    private Set<Book> books;

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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
        System.out.println("PRE PERSIST CALLBACK ON AUTHOR");
    }


    @PostLoad
    public void postLoad() {
        assert id != null : "id should not be null when post load hook is called";
        assert lastName != null : "last name should not be null when post load hook is called";

        posLoadHookCalled = true;
        System.out.println("POST LOAD CALLBACK ON AUTHOR");
    }

    public boolean wasPrePersistHookCalled() {
        return prePersistCalled;
    }

    public boolean wasPostLoadHookCalled() {
        return posLoadHookCalled;
    }
}
