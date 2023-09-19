package com.bookstore.service;

import com.bookstore.dto.order.OrderCreateDto;
import com.bookstore.dto.order.OrderResponseDto;
import com.bookstore.dto.order.OrderUpdateDto;
import com.bookstore.dto.orderitem.OrderItemResponseDto;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderResponseDto placeOrder(Long userId, OrderCreateDto orderCreateDto);

    void updateOrderStatus(Long id, OrderUpdateDto orderUpdateDto);

    OrderItemResponseDto getOrderItemWithinAnOrder(Long orderId, Long itemId, Long userId);

    List<OrderResponseDto> getAllUserOrders(Long userId, Pageable pageable);

    List<OrderItemResponseDto> getAllOrderItemsByOrderId(Long orderId,
                                                         Long userId,
                                                         Pageable pageable);
}
