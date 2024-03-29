package com.bookstore.repository.cartitem;

import com.bookstore.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    void deleteCartItemsByShoppingCartId(Long shoppingCartId);
}
