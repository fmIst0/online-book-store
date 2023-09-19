package com.bookstore.service.impl;

import com.bookstore.dto.order.OrderCreateDto;
import com.bookstore.dto.order.OrderResponseDto;
import com.bookstore.dto.order.OrderUpdateDto;
import com.bookstore.dto.orderitem.OrderItemResponseDto;
import com.bookstore.exception.EntityNotFoundException;
import com.bookstore.mapper.OrderItemMapper;
import com.bookstore.mapper.OrderMapper;
import com.bookstore.model.Order;
import com.bookstore.model.OrderItem;
import com.bookstore.model.ShoppingCart;
import com.bookstore.repository.order.OrderRepository;
import com.bookstore.repository.orderitem.OrderItemRepository;
import com.bookstore.service.OrderService;
import com.bookstore.service.ShoppingCartService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ShoppingCartService shoppingCartService;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final OrderItemRepository orderItemRepository;

    @Override
    public OrderResponseDto placeOrder(Long userId, OrderCreateDto orderCreateDto) {
        ShoppingCart shoppingCart = shoppingCartService.getShoppingCartByUserId(userId);
        Order newOrder = orderMapper.shoppingCartToOrder(shoppingCart);
        newOrder.setTotal(shoppingCart.getTotal());
        newOrder.setShippingAddress(orderCreateDto.getShippingAddress());

        Order savedOrder = orderRepository.save(newOrder);

        Set<OrderItem> orderItemSet = getOrderItemsFromCart(shoppingCart);
        setOrderForOrderItems(orderItemSet, savedOrder);
        savedOrder.setOrderItems(saveOrderItems(orderItemSet));
        shoppingCartService.cleanShoppingCart(shoppingCart);
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public void updateOrderStatus(Long orderId, OrderUpdateDto orderUpdateDto) {
        Order order = findOrderById(orderId);
        order.setStatus(orderUpdateDto.getStatus());
        orderRepository.save(order);
    }

    @Override
    public OrderItemResponseDto getOrderItemWithinAnOrder(Long orderId, Long itemId, Long userId) {
        return orderItemMapper.toDto(
                orderItemRepository
                        .findOrderItemByOrderIdAndByItemIdAndByUserId(orderId, itemId, userId)
        );
    }

    @Override
    public List<OrderResponseDto> getAllUserOrders(Long userId, Pageable pageable) {
        return orderRepository.getAllByUserId(userId, pageable)
                .stream()
                .map(orderMapper::toDto)
                .toList();
    }

    @Override
    public List<OrderItemResponseDto> getAllOrderItemsByOrderId(Long orderId,
                                                                Long userId,
                                                                Pageable pageable) {
        return orderItemRepository.findOrderItemsByOrderIdAndByUserId(orderId, userId, pageable)
                .stream()
                .map(orderItemMapper::toDto)
                .toList();
    }

    private Set<OrderItem> getOrderItemsFromCart(ShoppingCart shoppingCart) {
        return shoppingCart.getCartItems()
                .stream()
                .map(orderItemMapper::cartItemToOrderItem)
                .collect(Collectors.toSet());
    }

    private void setOrderForOrderItems(Set<OrderItem> orderItemSet, Order order) {
        orderItemSet.forEach(orderItem -> orderItem.setOrder(order));
    }

    private Set<OrderItem> saveOrderItems(Set<OrderItem> orderItemSet) {
        return orderItemSet.stream()
                .map(orderItemRepository::save)
                .collect(Collectors.toSet());
    }

    private Order findOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Can't find order in DB by id: " + orderId));
    }
}
