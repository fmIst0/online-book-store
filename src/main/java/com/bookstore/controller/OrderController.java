package com.bookstore.controller;

import com.bookstore.dto.order.OrderCreateDto;
import com.bookstore.dto.order.OrderResponseDto;
import com.bookstore.dto.order.OrderUpdateDto;
import com.bookstore.dto.orderitem.OrderItemResponseDto;
import com.bookstore.model.User;
import com.bookstore.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Order management", description = "Endpoints for managing orders")
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/orders")
public class OrderController {
    private final OrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('USER')")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Place an order",
            description = "Place an order. Create a new order from shopping cart")
    public OrderResponseDto createOrder(Authentication authentication,
                                        @RequestBody @Valid OrderCreateDto orderCreateDto) {
        User user = (User) authentication.getPrincipal();
        return orderService.placeOrder(user.getId(), orderCreateDto);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Retrieve user's order history",
            description = "User can see all his orders history")
    public List<OrderResponseDto> getAllUsersOrders(Authentication authentication,
                                                    Pageable pageable) {
        User user = (User) authentication.getPrincipal();
        return orderService.getAllUserOrders(user.getId(), pageable);
    }

    @GetMapping(value = "/{orderId}/items")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Retrieve all OrderItems for a specific order",
            description = "Retrieve all OrderItems for a specific order")
    public List<OrderItemResponseDto> getAllOrderItemsByOrderId(Authentication authentication,
                                                                @PathVariable Long orderId,
                                                                Pageable pageable) {
        User user = (User) authentication.getPrincipal();
        return orderService.getAllOrderItemsByOrderId(orderId, user.getId(), pageable);
    }

    @GetMapping(value = "/{orderId}/items/{itemId}")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Retrieve a specific OrderItem within an order",
            description = "Retrieve a specific OrderItem within an order")
    public OrderItemResponseDto getOrderItemWithinAnOrder(Authentication authentication,
                                                          @PathVariable Long orderId,
                                                          @PathVariable Long itemId) {
        User user = (User) authentication.getPrincipal();
        return orderService.getOrderItemWithinAnOrder(orderId, itemId, user.getId());
    }

    @PatchMapping(value = "/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(summary = "Update order status(for admins)",
            description = "Update order status")
    public void updateOrderStatus(@PathVariable Long id,
                                  @RequestBody @Valid OrderUpdateDto orderUpdateDto) {
        orderService.updateOrderStatus(id, orderUpdateDto);
    }
}
