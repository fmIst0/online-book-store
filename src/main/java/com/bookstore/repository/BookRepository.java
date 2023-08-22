package com.bookstore.repository;

import com.bookstore.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    Book save(Book book);

    Optional<Book> findBookById(Long id);

    List<Book> findAll();
}
