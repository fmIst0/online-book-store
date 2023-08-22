package com.bookstore.service;

import com.bookstore.dto.BookDto;
import com.bookstore.dto.CreateBookRequestDto;
import java.util.List;

public interface BookService {
    BookDto createBook(CreateBookRequestDto bookRequestDto);

    BookDto getBookById(Long id);

    List<BookDto> getAll();
}
