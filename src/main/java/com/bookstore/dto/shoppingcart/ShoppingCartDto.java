package com.bookstore.dto.shoppingcart;

import com.bookstore.dto.cartitem.CartItemResponseDto;
import java.util.Set;
import lombok.Data;

@Data
public class ShoppingCartDto {
    private Long userId;
    private Set<CartItemResponseDto> cartItemsDto;
}
