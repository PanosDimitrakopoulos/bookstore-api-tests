package com.bookstore.api.model;

public record Author(
        Integer id,
        Integer idBook,
        String firstName,
        String lastName) {

    public Author withId(Integer newId) {
        return new Author(newId, idBook, firstName, lastName);
    }

    public Author withIdBook(Integer newIdBook) {
        return new Author(id, newIdBook, firstName, lastName);
    }

    public Author withFirstName(String newFirstName) {
        return new Author(id, idBook, newFirstName, lastName);
    }

    public Author withLastName(String newLastName) {
        return new Author(id, idBook, firstName, newLastName);
    }
}
