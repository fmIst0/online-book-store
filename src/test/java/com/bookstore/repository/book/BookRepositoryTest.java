package com.bookstore.repository.book;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.bookstore.model.Book;
import com.bookstore.repository.category.CategoryRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BookRepositoryTest {
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Find all books by category id")
    @Sql(scripts = {
            "classpath:database/books-repo/add-book-to-books-table.sql",
            "classpath:database/books-repo/add-test-category-for-repo.sql",
            "classpath:database/"
                    + "books_categories/"
                    + "add-bookId-and-categoryId-to-books_categories-table.sql"
            }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/"
                    + "books_categories/"
                    + "remove-bookId-and-categoryId-from-books_categories-table.sql",
            "classpath:database/books-repo/remove-book-from-books-table.sql",
            "classpath:database/categories/remove-category-from-categories-table.sql"
            }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findAllByCategoryId_ValidCategoryId_ReturnsValidBooksList() {
        List<Book> actual = bookRepository.findAllByCategoryId(1L,
                PageRequest.of(0,10));

        assertEquals(1, actual.size());
    }
}
