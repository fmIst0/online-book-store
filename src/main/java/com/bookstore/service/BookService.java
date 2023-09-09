package com.bookstore.service;

import com.bookstore.dto.book.BookDto;
import com.bookstore.dto.book.BookDtoWithoutCategoryIds;
import com.bookstore.dto.book.BookSearchParametersDto;
import com.bookstore.dto.book.CreateBookRequestDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookDto createBook(CreateBookRequestDto bookRequestDto);

    BookDto getBookById(Long id);

    List<BookDto> getAll(Pageable pageable);

    void updateBook(Long id, CreateBookRequestDto bookRequestDto);

    void deleteBookById(Long id);

    List<BookDto> searchBooks(BookSearchParametersDto searchParameters);

    List<BookDtoWithoutCategoryIds> findAllByCategoryId(Long categoryId, Pageable pageable);
}
