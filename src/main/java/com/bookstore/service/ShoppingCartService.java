package com.bookstore.service;

import com.bookstore.dto.cartitem.CartItemCreateDto;
import com.bookstore.dto.cartitem.CartItemUpdateDto;
import com.bookstore.dto.shoppingcart.ShoppingCartDto;
import com.bookstore.model.ShoppingCart;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface ShoppingCartService {
    List<ShoppingCartDto> findAll(Pageable pageable);

    ShoppingCartDto getByUserId(Long id);

    ShoppingCartDto saveBookToTheCart(Long id, CartItemCreateDto cartItemCreateDto);

    ShoppingCartDto update(Long id, Long cartItemId, CartItemUpdateDto cartItemUpdateDto);

    void deleteCartItemFromTheCart(Long id);

    ShoppingCart getShoppingCartByUserId(Long id);

    void cleanShoppingCart(ShoppingCart shoppingCart);
}
