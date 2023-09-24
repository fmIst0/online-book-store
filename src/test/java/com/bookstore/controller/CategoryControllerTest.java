package com.bookstore.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.dto.book.BookDtoWithoutCategoryIds;
import com.bookstore.dto.category.CategoryDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
class CategoryControllerTest {
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
        teardown(dataSource);
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/categories/add-three-default-categories.sql")
            );
        }
    }

    @AfterAll
    static void afterAll(
            @Autowired DataSource dataSource
    ) {
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/categories/remove-all-from-categories.sql")
            );
        }
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Create a new category")
    @Sql(scripts = {
            "classpath:database/categories/remove-category-from-categories-table.sql"
            }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void createCategory_ValidRequestDto_Success() throws Exception {
        //Given
        CategoryDto expected = new CategoryDto()
                .setName("Test Category")
                .setDescription("Test Category Description");

        String jsonRequest = objectMapper.writeValueAsString(expected);

        //When
        MvcResult result = mockMvc.perform(post("/api/categories")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isCreated())
                .andReturn();

        //Then
        CategoryDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                CategoryDto.class);

        assertNotNull(actual);
        EqualsBuilder.reflectionEquals(expected, actual, "id");
    }

    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    @Test
    @DisplayName("Get all categories")
    void getAll_GivenCategoriesInCatalog_ShouldReturnAllCategories() throws Exception {
        List<CategoryDto> expected = new ArrayList<>();
        expected.add(new CategoryDto().setName("Name1").setDescription("Desc1"));
        expected.add(new CategoryDto().setName("Name2").setDescription("Desc2"));
        expected.add(new CategoryDto().setName("Name3").setDescription("Desc3"));

        //When
        MvcResult result = mockMvc.perform(get("/api/categories")
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        CategoryDto[] actual = objectMapper.readValue(result.getResponse().getContentAsByteArray(),
                CategoryDto[].class);
        assertEquals(expected.size(), actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    @Test
    @DisplayName("Get category by specific id")
    void getCategoryById_ValidId_ShouldReturnCategoryDto() throws Exception {
        //Given
        Long categoryId = 1L;
        CategoryDto expected = new CategoryDto().setName("Name1").setDescription("Desc1");

        //When
        MvcResult result = mockMvc.perform(get("/api/categories/" + categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        CategoryDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                CategoryDto.class);
        EqualsBuilder.reflectionEquals(expected, actual);
    }

    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    @Test
    @DisplayName("Should return bad request with invalid id")
    void getCategoryById_InvalidId_ShouldReturnCategoryDto() throws Exception {
        //Given
        Long categoryId = -1L;

        //When
        MvcResult result = mockMvc.perform(get("/api/categories/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();

    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Update category")
    @Sql(scripts = {
            "classpath:database/categories/add-category-to-categories-table.sql"
            }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/categories/remove-category-from-categories-table.sql"
            }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateCategory_ValidIdAndCategoryDto_ShouldReturnCategoryDto() throws Exception {
        //Given
        Long categoryId = -100L;

        CategoryDto expected = new CategoryDto()
                .setName("Test Category")
                .setDescription("Test Category Description");

        String jsonRequest = objectMapper.writeValueAsString(expected);

        //When
        MvcResult result = mockMvc.perform(put("/api/categories/" + categoryId)
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isAccepted())
                .andReturn();

        //Then
        CategoryDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                CategoryDto.class);
        assertEquals(expected, actual);
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Should return bad request with invalid id and requestDto")
    void updateCategory_InvalidIdAndCategoryDto_ShouldReturnBadRequest() throws Exception {
        //Given
        Long categoryId = -1L;

        CategoryDto expected = new CategoryDto()
                .setName("Name1")
                .setDescription("Desc1");

        String jsonRequest = objectMapper.writeValueAsString(expected);

        //When
        MvcResult result = mockMvc.perform(put("/api/categories/" + categoryId)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Delete a category from db")
    void deleteCategory_ValidId_ShouldDeleteCategoryFromDb() throws Exception {
        //Given
        Long categoryId = 1L;

        //When
        MvcResult result = mockMvc.perform(delete("/api/categories/" + categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Should return bad request with invalid id")
    void deleteCategory_InvalidId_ShouldReturnBadRequest() throws Exception {
        //Given
        Long categoryId = -1L;

        //When
        MvcResult result = mockMvc.perform(delete("/api/categories/" + categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    @Test
    @Sql(scripts = {
            "classpath:database/books-controller/add-three-default-books.sql",
            "classpath:database/books_categories/add-books-for-category.sql"
            }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/books_categories/remove-all-from-books-categories.sql",
            "classpath:database/books-controller/delete-from-books.sql"
            }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void getBooksByCategoryId_ValidId_ShouldReturnBookDtosWithoutCategoryIds() throws Exception {
        //Given
        Long categoryId = 1L;

        BookDtoWithoutCategoryIds book1 = new BookDtoWithoutCategoryIds()
                .setId(1L).setTitle("Test Title").setAuthor("Test Author")
                .setIsbn("123").setPrice(BigDecimal.valueOf(49.95))
                .setDescription("Test Description").setCoverImage("Cover Image");
        BookDtoWithoutCategoryIds book2 = new BookDtoWithoutCategoryIds()
                .setId(2L).setTitle("Test Title1").setAuthor("Test Author1")
                .setIsbn("1234").setPrice(BigDecimal.valueOf(99.99))
                .setDescription("Test Description1").setCoverImage("Cover Image1");
        BookDtoWithoutCategoryIds book3 = new BookDtoWithoutCategoryIds()
                .setId(3L).setTitle("Test Title2").setAuthor("Test Author2")
                .setIsbn("12345").setPrice(BigDecimal.valueOf(10.49))
                .setDescription("Test Description2").setCoverImage("Cover Image2");

        List<BookDtoWithoutCategoryIds> expected = new ArrayList<>();
        expected.add(book1);
        expected.add(book2);
        expected.add(book3);

        //When
        MvcResult result = mockMvc.perform(get("/api/categories/" + categoryId + "/books")
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        BookDtoWithoutCategoryIds[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                BookDtoWithoutCategoryIds[].class);
        assertEquals(expected.size(), actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }
}
