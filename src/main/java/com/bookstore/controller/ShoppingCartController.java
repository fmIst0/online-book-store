package com.bookstore.controller;

import com.bookstore.dto.cartitem.CartItemCreateDto;
import com.bookstore.dto.cartitem.CartItemUpdateDto;
import com.bookstore.dto.shoppingcart.ShoppingCartDto;
import com.bookstore.model.User;
import com.bookstore.service.ShoppingCartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Shopping cart management", description = "Endpoints for managing shopping carts")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/cart")
public class ShoppingCartController {
    private final ShoppingCartService shoppingCartService;

    @GetMapping(value = "/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all shopping carts",
            description = "Get all shopping carts only for admins")
    public List<ShoppingCartDto> getAll(Pageable pageable) {
        return shoppingCartService.findAll(pageable);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Retrieve user's shopping cart",
            description = "Retrieve user's shopping cart")
    public ShoppingCartDto getShoppingCartByUser(Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.getByUserId(user.getId());
    }

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Add book to the shopping cart",
            description = "Add book to the shopping cart")
    public ShoppingCartDto addBookToTheCart(Authentication authentication,
                                            @RequestBody @Valid
                                            CartItemCreateDto cartItemCreateDto) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.saveBookToTheCart(user.getId(), cartItemCreateDto);
    }

    @PutMapping(value = "/cart-items/{cartItemId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Update quantity of a book in the shopping cart",
            description = "Update quantity of a book in the shopping cart")
    public ShoppingCartDto updateBookInTheCart(Authentication authentication,
                                               @PathVariable Long cartItemId,
                                               @RequestBody @Valid
                                                   CartItemUpdateDto cartItemUpdateDto) {
        User user = (User) authentication.getPrincipal();
        return shoppingCartService.update(user.getId(), cartItemId, cartItemUpdateDto);
    }

    @DeleteMapping(value = "/cart-items/{cartItemId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Remove a book from the shopping cart",
            description = "Remove a book from the shopping cart")
    public void removeBookFromTheCart(@PathVariable Long cartItemId) {
        shoppingCartService.deleteCartItemFromTheCart(cartItemId);
    }
}
