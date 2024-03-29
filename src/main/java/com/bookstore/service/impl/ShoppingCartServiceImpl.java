package com.bookstore.service.impl;

import com.bookstore.dto.cartitem.CartItemCreateDto;
import com.bookstore.dto.cartitem.CartItemUpdateDto;
import com.bookstore.dto.shoppingcart.ShoppingCartDto;
import com.bookstore.exception.EntityNotFoundException;
import com.bookstore.mapper.CartItemMapper;
import com.bookstore.mapper.ShoppingCartMapper;
import com.bookstore.model.Book;
import com.bookstore.model.CartItem;
import com.bookstore.model.ShoppingCart;
import com.bookstore.repository.book.BookRepository;
import com.bookstore.repository.cartitem.CartItemRepository;
import com.bookstore.repository.shoppingcart.ShoppingCartRepository;
import com.bookstore.service.ShoppingCartService;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final CartItemMapper cartItemMapper;
    private final CartItemRepository cartItemRepository;
    private final BookRepository bookRepository;

    @Override
    public List<ShoppingCartDto> findAll(Pageable pageable) {
        return shoppingCartRepository.findAll(pageable)
                .stream()
                .map(shoppingCartMapper::toDto)
                .toList();
    }

    @Override
    public ShoppingCartDto getByUserId(Long id) {
        return shoppingCartMapper.toDto(getShoppingCartByUserId(id));
    }

    @Override
    public ShoppingCartDto saveBookToTheCart(Long userId, CartItemCreateDto cartItemCreateDto) {
        ShoppingCart shoppingCart = getShoppingCartByUserId(userId);
        Book book = bookRepository.findById(cartItemCreateDto.getBookId()).orElseThrow(() ->
                new EntityNotFoundException(
                        "Can't find a book in DB by id: " + cartItemCreateDto.getBookId()
                ));
        CartItem cartItem = cartItemMapper.toEntity(cartItemCreateDto, book, shoppingCart);
        cartItemRepository.save(cartItem);
        shoppingCart.addCartItemToSet(cartItem);
        return shoppingCartMapper.toDto(shoppingCart);
    }

    @Override
    public ShoppingCartDto update(Long userId,
                                  Long cartItemId,
                                  CartItemUpdateDto cartItemUpdateDto) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Can't find a cart item in DB by id: " + cartItemId
                        ));
        cartItem.setQuantity(cartItemUpdateDto.getQuantity());
        cartItemRepository.save(cartItem);
        return shoppingCartMapper.toDto(getShoppingCartByUserId(userId));
    }

    @Override
    public void deleteCartItemFromTheCart(Long id) {
        if (cartItemRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Can't delete a cart item from DB with id: " + id);
        }
        cartItemRepository.deleteById(id);
    }

    @Override
    public ShoppingCart getShoppingCartByUserId(Long id) {
        return shoppingCartRepository.findByUserId(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Can't find a shopping cart by user id: " + id
                        ));
    }

    @Override
    @Transactional
    public void cleanShoppingCart(ShoppingCart shoppingCart) {
        shoppingCart.setCartItems(new HashSet<>());
        cartItemRepository.deleteCartItemsByShoppingCartId(shoppingCart.getId());
    }
}
