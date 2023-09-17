package com.bookstore.repository.orderitem;

import com.bookstore.model.OrderItem;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findOrderItemsByOrderId(Long orderId, Pageable pageable);

    @Query(value = "FROM OrderItem oi "
            + "WHERE oi.order.id = :orderId "
            + "AND oi.id = :itemId")
    OrderItem findOrderItemByOrderIdAndByItemId(Long orderId, Long itemId);
}
