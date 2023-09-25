package com.bookstore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.dto.book.BookDto;
import com.bookstore.dto.book.CreateBookRequestDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import javax.sql.DataSource;
import lombok.SneakyThrows;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookControllerTest {
    protected static MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired DataSource dataSource,
            @Autowired WebApplicationContext applicationContext
    ) throws SQLException {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(springSecurity())
                .build();
        tearDown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books-controller/add-three-default-books.sql")
            );
        }
    }

    @AfterAll
    static void afterAll(
            @Autowired DataSource dataSource
    ) {
        tearDown(dataSource);
    }

    @SneakyThrows
    static void tearDown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books-controller/delete-from-books.sql")
            );
        }
    }

    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    @Test
    @DisplayName("Get all books")
    void getAll_GivenBooksInCatalog_ShouldReturnAllBooks() throws Exception {
        //Given
        List<BookDto> expected = new ArrayList<>();
        expected.add(new BookDto().setId(1L).setTitle("Test Title").setAuthor("Test Author")
                .setIsbn("123").setPrice(BigDecimal.valueOf(49.95))
                .setDescription("Test Description").setCoverImage("Cover Image")
                .setCategoryIds(new HashSet<>()));
        expected.add(new BookDto().setId(2L).setTitle("Test Title1").setAuthor("Test Author1")
                .setIsbn("1234").setPrice(BigDecimal.valueOf(99.99))
                .setDescription("Test Description1").setCoverImage("Cover Image1")
                .setCategoryIds(new HashSet<>()));
        expected.add(new BookDto().setId(3L).setTitle("Test Title2").setAuthor("Test Author2")
                .setIsbn("12345").setPrice(BigDecimal.valueOf(10.49))
                .setDescription("Test Description2").setCoverImage("Cover Image2")
                .setCategoryIds(new HashSet<>()));

        //When
        MvcResult result = mockMvc.perform(get("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        BookDto[] actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(),
                BookDto[].class);

        assertEquals(expected.size(), actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    @Test
    @DisplayName("Get book by specific id")
    void getBookById_ValidId_ShouldReturnBookBySpecificId() throws Exception {
        //Given
        Long bookId = 1L;
        BookDto expected = getBookDto();

        //When
        MvcResult result = mockMvc.perform(get("/api/books/" + bookId)
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        BookDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                BookDto.class);
        EqualsBuilder.reflectionEquals(expected, actual);
    }

    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    @Test
    @DisplayName("searchBooks() method works with price between")
    void searchBooks_ValidSearchParameters_ShouldReturnValidBookDtos() throws Exception {
        //Given
        List<BookDto> expected = new ArrayList<>();
        expected.add(new BookDto().setId(1L).setTitle("Test Title").setAuthor("Test Author")
                .setIsbn("123").setPrice(BigDecimal.valueOf(49.95))
                .setDescription("Test Description").setCoverImage("Cover Image")
                .setCategoryIds(new HashSet<>()));

        //When
        MvcResult result = mockMvc.perform(get("/api/books/search?prices=40,50")
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        BookDto[] actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                BookDto[].class);

        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Create a new Book")
    @Sql(scripts = {
            "classpath:database/books-controller/delete-test-book-from-books.sql"
            }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createBook_ValidRequestDto_Success() throws Exception {
        //Given
        CreateBookRequestDto createBookRequestDto = new CreateBookRequestDto()
                .setTitle("Test Book")
                .setPrice(BigDecimal.TEN)
                .setDescription("Test Description")
                .setCoverImage("coverImage")
                .setAuthor("Tester")
                .setIsbn("793473")
                .setCategoryIds(new HashSet<>());

        String jsonRequest = objectMapper.writeValueAsString(createBookRequestDto);

        //When
        MvcResult result = mockMvc.perform(
                post("/api/books")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        BookDto expected = new BookDto()
                .setAuthor(createBookRequestDto.getAuthor())
                .setIsbn(createBookRequestDto.getIsbn())
                .setDescription(createBookRequestDto.getDescription())
                .setPrice(createBookRequestDto.getPrice())
                .setCoverImage(createBookRequestDto.getCoverImage())
                .setCategoryIds(createBookRequestDto.getCategoryIds())
                .setTitle(createBookRequestDto.getTitle());

        BookDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                BookDto.class);
        assertNotNull(actual);
        assertNotNull(actual.getId());
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Update book")
    @Sql(scripts = {
            "classpath:database/books-controller/insert-test-book-to-books.sql"
            }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/books-controller/delete-test-book-from-books.sql"
            }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateBook_ValidBookIdAndRequestDto_ShouldUpdateBookValuesAndReturnBookDto()
            throws Exception {
        //Given
        Long bookId = -100L;

        CreateBookRequestDto createBookRequestDto = new CreateBookRequestDto()
                .setTitle("Test Book")
                .setPrice(BigDecimal.TEN)
                .setDescription("Test Description")
                .setCoverImage("coverImage")
                .setAuthor("Tester")
                .setIsbn("459473")
                .setCategoryIds(new HashSet<>());

        BookDto expected = new BookDto()
                .setAuthor(createBookRequestDto.getAuthor())
                .setIsbn(createBookRequestDto.getIsbn())
                .setDescription(createBookRequestDto.getDescription())
                .setPrice(createBookRequestDto.getPrice())
                .setCoverImage(createBookRequestDto.getCoverImage())
                .setCategoryIds(createBookRequestDto.getCategoryIds())
                .setTitle(createBookRequestDto.getTitle());

        String jsonRequest = objectMapper.writeValueAsString(createBookRequestDto);

        //When
        MvcResult result = mockMvc.perform(put("/api/books/" + bookId)
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isAccepted())
                .andReturn();

        //Then
        BookDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                BookDto.class);
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Delete book by specific id")
    void deleteBookById_ValidId_ShouldDeleteBookFromDb() throws Exception {
        //Given
        Long bookId = 1L;

        //When
        MvcResult result = mockMvc.perform(delete("/api/books/" + bookId)
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Delete book by specific id")
    void deleteBookById_InvalidId_ShouldReturnBadRequest() throws Exception {
        //Given
        Long bookId = -1L;

        //When
        MvcResult result = mockMvc.perform(delete("/api/books/" + bookId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    private BookDto getBookDto() {
        return new BookDto().setId(1L).setTitle("Test Title").setAuthor("Test Author")
                .setIsbn("isbn").setPrice(BigDecimal.valueOf(49.95))
                .setDescription("Test Description").setCoverImage("Cover Image")
                .setCategoryIds(new HashSet<>());
    }
}
