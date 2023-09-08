package com.bookstore.service.impl;

import com.bookstore.dto.book.BookDto;
import com.bookstore.dto.book.BookDtoWithoutCategoryIds;
import com.bookstore.dto.book.BookSearchParametersDto;
import com.bookstore.dto.book.CreateBookRequestDto;
import com.bookstore.exception.EntityNotFoundException;
import com.bookstore.mapper.BookMapper;
import com.bookstore.model.Book;
import com.bookstore.model.Category;
import com.bookstore.repository.book.BookRepository;
import com.bookstore.repository.book.BookSpecificationBuilder;
import com.bookstore.repository.category.CategoryRepository;
import com.bookstore.service.BookService;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;
    private final BookMapper bookMapper;
    private final BookSpecificationBuilder bookSpecificationBuilder;

    @Override
    public BookDto createBook(CreateBookRequestDto bookRequestDto) {
        Set<Category> categories = categoryRepository.findByIdIn(bookRequestDto.getCategoryIds());
        Book book = bookMapper.toBookModel(bookRequestDto);
        book.setCategories(categories);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public BookDto getBookById(Long id) {
        return bookRepository.findById(id)
                .map(bookMapper::toDto)
                .orElseThrow(() ->
                        new EntityNotFoundException("No book in DB by id: " + id));
    }

    @Override
    public List<BookDto> getAll(Pageable pageable) {
        return bookRepository.findAllPageable(pageable)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public void updateBook(Long id, CreateBookRequestDto bookRequestDto) {
        Set<Category> categories = categoryRepository.findByIdIn(bookRequestDto.getCategoryIds());
        Book bookFromDb = bookRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("No book in DB by id: " + id));
        bookFromDb.setTitle(bookRequestDto.getTitle());
        bookFromDb.setAuthor(bookRequestDto.getAuthor());
        bookFromDb.setPrice(bookRequestDto.getPrice());
        bookFromDb.setIsbn(bookRequestDto.getIsbn());
        bookFromDb.setDescription(bookRequestDto.getDescription());
        bookFromDb.setCoverImage(bookRequestDto.getCoverImage());
        bookFromDb.setCategories(categories);
        bookRepository.save(bookFromDb);
    }

    @Override
    public void deleteBookById(Long id) {
        bookRepository.deleteById(id);
    }

    @Override
    public List<BookDto> searchBooks(BookSearchParametersDto searchParameters) {
        Specification<Book> bookSpecification = bookSpecificationBuilder.build(searchParameters);
        return bookRepository.findAll(bookSpecification)
                .stream()
                .map(bookMapper::toDto)
                .toList();
    }

    @Override
    public List<BookDtoWithoutCategoryIds> findAllByCategoryId(Long categoryId, Pageable pageable) {
        return bookRepository.findAllByCategoryId(categoryId, pageable)
                .stream()
                .map(bookMapper::toDtoWithoutCategories)
                .toList();
    }
}
