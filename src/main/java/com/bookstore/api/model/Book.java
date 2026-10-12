package com.bookstore.api.model;

import java.time.OffsetDateTime;

public record Book(
        Integer id,
        String title,
        String description,
        Integer pageCount,
        String excerpt,
        OffsetDateTime publishDate) {

    public Book withId(Integer newId) {
        return new Book(newId, title, description, pageCount, excerpt, publishDate);
    }

    public Book withTitle(String newTitle) {
        return new Book(id, newTitle, description, pageCount, excerpt, publishDate);
    }

    public Book withDescription(String newDescription) {
        return new Book(id, title, newDescription, pageCount, excerpt, publishDate);
    }

    public Book withPageCount(Integer newPageCount) {
        return new Book(id, title, description, newPageCount, excerpt, publishDate);
    }

    public Book withExcerpt(String newExcerpt) {
        return new Book(id, title, description, pageCount, newExcerpt, publishDate);
    }

    public Book withPublishDate(OffsetDateTime newPublishDate) {
        return new Book(id, title, description, pageCount, excerpt, newPublishDate);
    }
}
