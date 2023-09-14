package com.bookstore.service.impl;

import com.bookstore.dto.cartitem.CartItemCreateDto;
import com.bookstore.dto.cartitem.CartItemUpdateDto;
import com.bookstore.dto.shoppingcart.ShoppingCartDto;
import com.bookstore.exception.EntityNotFoundException;
import com.bookstore.mapper.CartItemMapper;
import com.bookstore.mapper.ShoppingCartMapper;
import com.bookstore.model.CartItem;
import com.bookstore.model.ShoppingCart;
import com.bookstore.model.User;
import com.bookstore.repository.cartitem.CartItemRepository;
import com.bookstore.repository.shoppingcart.ShoppingCartRepository;
import com.bookstore.repository.user.UserRepository;
import com.bookstore.service.ShoppingCartService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ShoppingCartServiceImpl implements ShoppingCartService {
    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final UserRepository userRepository;
    private final CartItemMapper cartItemMapper;
    private final CartItemRepository cartItemRepository;

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
        CartItem cartItem = cartItemMapper.toEntity(cartItemCreateDto);
        cartItem.setShoppingCart(shoppingCart);
        cartItemRepository.save(cartItem);
        return shoppingCartMapper.toDto(getShoppingCartByUserId(userId));
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
        return getByUserId(userId);
    }

    @Override
    public void deleteCartItemFromTheCart(Long id) {
        cartItemRepository.deleteById(id);
    }

    private ShoppingCart getShoppingCartByUserId(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Can't find user in DB by id: " + id));
        return shoppingCartRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Can't find a shopping cart by id: " + user.getId()
                        ));
    }
}
