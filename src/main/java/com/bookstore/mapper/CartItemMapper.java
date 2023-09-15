package com.bookstore.mapper;

import com.bookstore.config.MapperConfig;
import com.bookstore.dto.cartitem.CartItemCreateDto;
import com.bookstore.dto.cartitem.CartItemResponseDto;
import com.bookstore.model.Book;
import com.bookstore.model.CartItem;
import com.bookstore.model.ShoppingCart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapperConfig.class)
public interface CartItemMapper {
    @Mapping(target = "id", ignore = true)
    CartItem toEntity(CartItemCreateDto cartItemCreateDto,
                      Book book,
                      ShoppingCart shoppingCart);

    @Mapping(source = "cartItem.book.id", target = "bookId")
    @Mapping(source = "cartItem.book.title", target = "bookTitle")
    CartItemResponseDto toDto(CartItem cartItem);
}
