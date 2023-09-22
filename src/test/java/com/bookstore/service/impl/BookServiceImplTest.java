package com.bookstore.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

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
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private BookSpecificationBuilder bookSpecificationBuilder;
    @InjectMocks
    private BookServiceImpl bookService;

    @Test
    @DisplayName("Verify createBook() method works")
    public void createBook_ValidCreateBookRequestDto_ReturnsBookDto() {
        //Given
        CreateBookRequestDto requestDto = getCreateBookRequestDto();

        Book book = getBookByCreateDto(requestDto);

        BookDto bookDto = getBookDtoByBook(book);

        when(bookMapper.toBookModel(requestDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        //When
        BookDto savedBookDto = bookService.createBook(requestDto);

        //Then
        assertThat(savedBookDto).isEqualTo(bookDto);
        verify(bookMapper, times(1)).toBookModel(requestDto);
        verify(bookRepository, times(1)).save(book);
        verify(bookMapper, times(1)).toDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify getAll() method works")
    public void getAll_ValidPageable_ReturnsAllBooks() {
        //Given
        Book book = getBook();

        BookDto bookDto = getBookDtoByBook(book);

        Pageable pageable = PageRequest.of(0,10);
        List<Book> books = List.of(book);
        Page<Book> bookPage = new PageImpl<>(books, pageable, books.size());

        when(bookRepository.findAll(pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        //When
        List<BookDto> bookDtos = bookService.getAll(pageable);

        //Then
        assertThat(bookDtos).hasSize(1);
        assertThat(bookDtos.get(0)).isEqualTo(bookDto);

        verify(bookRepository, times(1)).findAll(pageable);
        verify(bookMapper, times(1)).toDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify getBookById() method works")
    public void getBookById_ValidId_ReturnsValidBookDto() {
        //Given
        Book book = getBook();

        BookDto expected = getBookDtoByBook(book);

        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(expected);

        //When
        BookDto actual = bookService.getBookById(book.getId());

        //Then
        assertEquals(expected, actual);

        verify(bookRepository, times(1)).findById(anyLong());
        verify(bookMapper, times(1)).toDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify the EntityNotFoundException was thrown when bookId is invalid")
    public void getBookById_InvalidId_ThrowsEntityNotFoundException() {
        //Given
        Long bookId = -1L;

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        //When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.getBookById(bookId));

        //Then
        String expected = "No book in DB by id: " + bookId;
        String actual = exception.getMessage();
        assertEquals(EntityNotFoundException.class, exception.getClass());
        assertEquals(expected, actual);

        verify(bookRepository, times(1)).findById(bookId);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify updateBook() method works")
    public void updateBook_ValidBookId_ReturnsValidBookDto() {
        //Given
        CreateBookRequestDto requestDto = getCreateBookRequestDto();

        Book book = getBookByCreateDto(requestDto);

        BookDto expected = getBookDtoByBook(book);

        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(expected);

        //When
        BookDto actual = bookService.updateBook(book.getId(), requestDto);

        //Then
        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(anyLong());
        verify(bookRepository, times(1)).save(book);
        verify(bookMapper, times(1)).toDto(book);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify the EntityNotFoundException was thrown when bookId is invalid")
    public void updateBook_InvalidBookId_ThrowsEntityNotFoundException() {
        //Given
        CreateBookRequestDto requestDto = getCreateBookRequestDto();

        Long bookId = -1L;

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        //When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.updateBook(bookId, requestDto));

        //Then
        String expected = "No book in DB by id: " + bookId;
        String actual = exception.getMessage();

        assertEquals(EntityNotFoundException.class, exception.getClass());
        assertEquals(expected, actual);
        verify(bookRepository, times(1)).findById(bookId);
        verifyNoMoreInteractions(bookRepository);
    }

    @Test
    @DisplayName("Verify deleteBookById() method works")
    public void deleteBookById_ValidBookId_DoesNotThrowEntityNotFoundException() {
        assertDoesNotThrow(() -> bookService.deleteBookById(1L));
    }

    @Test
    @DisplayName("Verify searchBooks() method works")
    void searchBooks_ValidBookSearchParametersDto_ReturnsValidBookDto() {
        //Given
        String[] parameters = new String[0];
        Specification<Book> specification = Specification.where(null);
        BookSearchParametersDto bookSearchParametersDto =
                new BookSearchParametersDto(parameters, parameters, parameters, parameters);

        Book book = getBook();
        List<Book> books = List.of(book);
        BookDto bookDto = getBookDtoByBook(book);
        List<BookDto> bookDtos = List.of(bookDto);

        Pageable pageable = Pageable.ofSize(10);

        Page<BookDto> expected = new PageImpl<>(bookDtos, pageable, bookDtos.size());

        when(bookSpecificationBuilder.build(bookSearchParametersDto)).thenReturn(specification);
        when(bookRepository.findAll(specification)).thenReturn(books);
        when(bookMapper.toDto(book)).thenReturn(bookDto);

        //When
        Page<BookDto> actual = bookService.searchBooks(bookSearchParametersDto);

        //Then
        assertThat(actual).isEqualTo(expected);
        verify(bookSpecificationBuilder, times(1)).build(bookSearchParametersDto);
        verify(bookRepository, times(1)).findAll(specification);
        verify(bookMapper, times(1)).toDto(book);
        verifyNoMoreInteractions(bookSpecificationBuilder, bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify findAllByCategoryId() method works")
    public void findAllByCategoryId_ValidCategoryId_ReturnsValidBookDtosWithoutCategoryIds() {
        // Given
        Category category = getCategory();

        Book book = getBook();
        book.setCategories(Set.of(category));

        BookDtoWithoutCategoryIds bookDtoWithoutCategoryIds =
                getBookDtoByBookWithoutCategoryIds(book);

        Pageable pageable = PageRequest.of(0, 10);
        List<Book> books = List.of(book);
        List<BookDtoWithoutCategoryIds> bookDtoWithoutCategoryIdsList =
                List.of(bookDtoWithoutCategoryIds);

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.of(category));
        when(bookRepository.findAllByCategoryId(anyLong(), eq(pageable))).thenReturn(books);
        when(bookMapper.toDtoWithoutCategories(book)).thenReturn(bookDtoWithoutCategoryIds);

        // When
        List<BookDtoWithoutCategoryIds> actual =
                bookService.findAllByCategoryId(category.getId(), pageable);

        // Then
        List<BookDtoWithoutCategoryIds> expected = List.copyOf(bookDtoWithoutCategoryIdsList);
        assertEquals(expected, actual);
        verify(categoryRepository, times(1)).findById(anyLong());
        verify(bookRepository, times(1)).findAllByCategoryId(anyLong(), eq(pageable));
        verify(bookMapper, times(1)).toDtoWithoutCategories(book);
        verifyNoMoreInteractions(categoryRepository, bookRepository, bookMapper);
    }

    @Test
    @DisplayName("Verify the EntityNotFoundException was thrown when categoryId is invalid")
    public void findAllByCategoryId_InvalidCategoryId_ThrowsEntityNotFoundException() {
        //Given
        Long categoryId = -1L;

        Pageable pageable = PageRequest.of(0,10);

        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        //When
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> bookService.findAllByCategoryId(categoryId, pageable));

        //Then
        String expected = "Can't find a category in DB by id: " + categoryId;
        String actual = exception.getMessage();

        assertEquals(EntityNotFoundException.class, exception.getClass());
        assertThat(actual).isEqualTo(expected);
        verify(categoryRepository, times(1)).findById(categoryId);
        verifyNoMoreInteractions(categoryRepository);
    }

    private BookDtoWithoutCategoryIds getBookDtoByBookWithoutCategoryIds(Book book) {
        return new BookDtoWithoutCategoryIds()
                .setId(book.getId())
                .setIsbn(book.getIsbn())
                .setPrice(book.getPrice())
                .setDescription(book.getDescription())
                .setCoverImage(book.getCoverImage())
                .setTitle(book.getTitle())
                .setAuthor(book.getAuthor());
    }

    private CreateBookRequestDto getCreateBookRequestDto() {
        return new CreateBookRequestDto()
                .setTitle("Test Book")
                .setPrice(BigDecimal.TEN)
                .setDescription("Test Description")
                .setCoverImage("coverImage")
                .setAuthor("Tester")
                .setIsbn("123")
                .setCategoryIds(new HashSet<>());
    }

    private Book getBookByCreateDto(CreateBookRequestDto requestDto) {
        return new Book()
                .setId(1L)
                .setTitle(requestDto.getTitle())
                .setPrice(requestDto.getPrice())
                .setDescription(requestDto.getDescription())
                .setCoverImage(requestDto.getCoverImage())
                .setAuthor(requestDto.getAuthor())
                .setIsbn(requestDto.getIsbn())
                .setCategories(requestDto.getCategoryIds()
                        .stream()
                        .map(id -> {
                            Category category = new Category();
                            category.setId(id);
                            return category;
                        })
                        .collect(Collectors.toSet()));
    }

    private Book getBook() {
        return new Book()
                .setId(1L)
                .setTitle("Test Book")
                .setPrice(BigDecimal.TEN)
                .setDescription("Test Description")
                .setCoverImage("coverImage")
                .setAuthor("Tester")
                .setIsbn("123")
                .setCategories(new HashSet<>());
    }

    private BookDto getBookDtoByBook(Book book) {
        return new BookDto()
                .setId(book.getId())
                .setIsbn(book.getIsbn())
                .setTitle(book.getTitle())
                .setAuthor(book.getAuthor())
                .setPrice(book.getPrice())
                .setDescription(book.getDescription())
                .setCoverImage(book.getCoverImage())
                .setCategoryIds(book.getCategories()
                        .stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet()));
    }

    private Category getCategory() {
        return new Category()
                .setId(1L)
                .setName("Test Category")
                .setDescription("Test category description");
    }
}
