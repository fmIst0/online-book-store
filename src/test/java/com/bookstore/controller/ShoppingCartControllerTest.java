package com.bookstore.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.dto.cartitem.CartItemCreateDto;
import com.bookstore.dto.cartitem.CartItemResponseDto;
import com.bookstore.dto.cartitem.CartItemUpdateDto;
import com.bookstore.dto.shoppingcart.ShoppingCartDto;
import com.bookstore.model.Role;
import com.bookstore.model.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
class ShoppingCartControllerTest {
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
                    new ClassPathResource("database/users/add-three-default-users.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource(
                            "database/shopping_carts/add-three-default-shopping-carts.sql")
            );
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
        teardown(dataSource);
    }

    @SneakyThrows
    static void teardown(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(true);
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/shopping_carts/delete-from-shopping_carts.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/users/delete-from-users.sql")
            );
            ScriptUtils.executeSqlScript(
                    connection,
                    new ClassPathResource("database/books-controller/delete-from-books.sql")
            );
        }
    }

    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @Test
    @DisplayName("Get all carts")
    void getAll_GivenCartsInCatalog_ShouldReturnAllCarts() throws Exception {
        //Given
        List<ShoppingCartDto> expected = new ArrayList<>();
        expected.add(new ShoppingCartDto().setUserId(1L).setCartItemsDto(new HashSet<>()));
        expected.add(new ShoppingCartDto().setUserId(2L).setCartItemsDto(new HashSet<>()));
        expected.add(new ShoppingCartDto().setUserId(3L).setCartItemsDto(new HashSet<>()));

        //When
        MvcResult result = mockMvc.perform(get("/api/cart/all")
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        ShoppingCartDto[] actual = objectMapper.readValue(
                result.getResponse().getContentAsByteArray(),
                ShoppingCartDto[].class);

        assertEquals(expected.size(), actual.length);
        assertEquals(expected, Arrays.stream(actual).toList());
    }

    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    @Test
    @DisplayName("Get user's shopping cart")
    void getShoppingCartByUser_ValidUserId_ReturnsUsersCart() throws Exception {
        //Given
        User user = getUser();
        ShoppingCartDto expected = getShoppingCartDto();

        //When
        MvcResult result = mockMvc.perform(get("/api/cart")
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(user))
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        ShoppingCartDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                ShoppingCartDto.class);

        EqualsBuilder.reflectionEquals(expected, actual);
    }

    @Sql(scripts = {
            "classpath:database/cart_items/delete-test-cart-item.sql"
            }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    @DisplayName("Add book to the cart")
    void addBookToTheCart_ValidCartItemCreateDto_ReturnsShoppingCartDto() throws Exception {
        //Given
        User user = getUser();

        CartItemCreateDto cartItemCreateDto = new CartItemCreateDto()
                .setBookId(1L)
                .setQuantity(10);

        CartItemResponseDto expected = new CartItemResponseDto()
                .setBookId(1L)
                .setBookTitle("Test Title")
                .setQuantity(cartItemCreateDto.getQuantity());

        String jsonRequest = objectMapper.writeValueAsString(cartItemCreateDto);

        //When
        MvcResult result = mockMvc.perform(post("/api/cart")
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(user))
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        ShoppingCartDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                ShoppingCartDto.class);

        assertThat(actual.getCartItemsDto()).hasSize(1);
        EqualsBuilder.reflectionEquals(expected,
                actual.getCartItemsDto().stream().toList().get(0), "id");
    }

    @Sql(scripts = {
            "classpath:database/cart_items/add-test-cart-item.sql"
            }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/cart_items/delete-updated-cart-items.sql"
            }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    void updateBookInTheCart_ValidCartItemUpdateDto_ReturnsShoppingCartDto() throws Exception {
        //Given
        User user = getUser();

        Long cartItemId = -1L;

        CartItemUpdateDto cartItemUpdateDto = new CartItemUpdateDto()
                .setQuantity(15);

        CartItemResponseDto expected = new CartItemResponseDto()
                .setBookId(1L)
                .setBookTitle("Test Title")
                .setQuantity(cartItemUpdateDto.getQuantity());

        String jsonRequest = objectMapper.writeValueAsString(cartItemUpdateDto);

        //When
        MvcResult result = mockMvc.perform(put("/api/cart//cart-items/" + cartItemId)
                .content(jsonRequest)
                .contentType(MediaType.APPLICATION_JSON)
                .with(user(user))
                )
                .andExpect(status().isOk())
                .andReturn();

        //Then
        ShoppingCartDto actual = objectMapper.readValue(result.getResponse().getContentAsString(),
                ShoppingCartDto.class);

        assertThat(actual.getCartItemsDto()).hasSize(1);
        EqualsBuilder.reflectionEquals(expected,
                actual.getCartItemsDto().stream().toList().get(0), "id");
    }

    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    @Sql(scripts = {
            "classpath:database/cart_items/add-test-cart-item.sql"
            }, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:database/cart_items/delete-test-cart-item.sql"
            }, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    @Test
    @DisplayName("Remove book from the cart")
    void removeBookFromTheCart_ValidCartItemId_DeletesABookFromTheCart() throws Exception {
        //Given
        Long cartItemId = -1L;

        //When
        MvcResult result = mockMvc.perform(delete("/api/cart/cart-items/" + cartItemId)
                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andReturn();
    }

    @WithMockUser(username = "user", roles = {"USER", "ADMIN"})
    @Test
    @DisplayName("Remove book from the cart")
    void removeBookFromTheCart_InvalidCartItemId_ShouldReturnBadRequest() throws Exception {
        //Given
        Long cartItemId = -100L;

        //When
        MvcResult result = mockMvc.perform(delete("/api/cart/cart-items/" + cartItemId)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    private ShoppingCartDto getShoppingCartDto() {
        return new ShoppingCartDto()
                .setUserId(1L)
                .setCartItemsDto(new HashSet<>());
    }

    private User getUser() {
        Set<Role> roles = new HashSet<>();
        Role role = new Role();
        role.setName(Role.RoleName.USER);
        roles.add(role);
        return new User()
                .setId(1L)
                .setRoles(roles);
    }
}
