package com.bookstore.repository.book;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bookstore.model.Book;
import com.bookstore.model.Category;
import com.bookstore.repository.category.CategoryRepository;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Find all books by category id")
    void findAllByCategoryId_ValidCategoryId_ReturnsValidBooksList() {
        Category category = getCategory();
        categoryRepository.save(category);

        Book book = getBook();
        book.setCategories(Set.of(category));
        bookRepository.save(book);

        List<Book> actual = bookRepository.findAllByCategoryId(category.getId(),
                PageRequest.of(0,10));

        assertEquals(1, actual.size());
    }

    private Book getBook() {
        return new Book()
                .setTitle("Test Book")
                .setPrice(BigDecimal.TEN)
                .setDescription("Test Description")
                .setCoverImage("coverImage")
                .setAuthor("Tester")
                .setIsbn("123")
                .setCategories(new HashSet<>());
    }

    private Category getCategory() {
        return new Category()
                .setName("Test Category")
                .setDescription("Test category description");
    }
}
