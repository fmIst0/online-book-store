package com.bookstore.repository.orderitem;

import com.bookstore.model.OrderItem;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @Query(value = "FROM OrderItem oi "
            + "WHERE oi.order.id = :orderId "
            + "AND oi.order.user.id = :userId")
    List<OrderItem> findOrderItemsByOrderIdAndByUserId(Long orderId,
                                                       Long userId,
                                                       Pageable pageable);

    @Query(value = "FROM OrderItem oi "
            + "WHERE oi.order.id = :orderId "
            + "AND oi.id = :itemId "
            + "AND oi.order.user.id = :userId")
    OrderItem findOrderItemByOrderIdAndByItemIdAndByUserId(Long orderId,
                                                           Long itemId,
                                                           Long userId);
}
