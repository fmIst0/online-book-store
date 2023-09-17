package com.bookstore.mapper;

import com.bookstore.config.MapperConfig;
import com.bookstore.dto.order.OrderResponseDto;
import com.bookstore.model.Order;
import com.bookstore.model.ShoppingCart;
import java.math.BigDecimal;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(config = MapperConfig.class, uses = OrderItemMapper.class)
public interface OrderMapper {
    @Mapping(target = "orderItems", source = "orderItems")
    @Mapping(target = "userId", source = "order.user.id")
    OrderResponseDto toDto(Order order);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "status", expression = "java(com.bookstore.model.Order.Status.PENDING)")
    @Mapping(target = "orderDate", expression = "java(java.time.LocalDateTime.now())")
    Order shoppingCartToOrder(ShoppingCart shoppingCart);

    @AfterMapping
    default void setOrderTotal(@MappingTarget Order order, ShoppingCart shoppingCart) {
        order.setTotal(getTotal(shoppingCart));
    }

    private BigDecimal getTotal(ShoppingCart shoppingCart) {
        return shoppingCart.getCartItems()
                .stream()
                .map(cartItem -> cartItem.getBook()
                        .getPrice()
                        .multiply(
                                BigDecimal.valueOf(cartItem.getQuantity())
                        )
                )
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
