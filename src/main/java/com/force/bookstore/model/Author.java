package com.force.bookstore.model;

import com.force.bookstore.service.Persistable;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * @author rbrainard
 */
@Entity
public class Author implements Persistable {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
	private String id;
    private String firstName;
    private String lastName;
    private Date birthDate;

    @OneToMany(mappedBy = "author")
    private List<Book> books;

    public List<Book> getBooks() {
        return books;
    }

    public void setBooks(List<Book> books) {
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
}
