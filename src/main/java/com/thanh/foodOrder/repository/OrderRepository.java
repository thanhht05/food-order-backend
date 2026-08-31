package com.thanh.foodorder.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.thanh.foodorder.domain.Order;
import com.thanh.foodorder.domain.User;
import com.thanh.foodorder.domain.Voucher;
import com.thanh.foodorder.dto.response.order.OrderHistoryProjection;
import com.thanh.foodorder.enums.OrderStatus;
import com.thanh.foodorder.enums.PaymentStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {
    List<Order> findByUser(User user);

    boolean existsByUserAndVoucher(User user, Voucher voucher);

    @Query(value = """
            SELECT
                u.id AS userId,
                u.full_name AS fullName,
                c.id AS cartId,

                o.id AS orderId,
                o.order_date AS orderDate,
                o.order_status AS orderStatus,

                o.total_price AS totalPrice,

                p.id AS productId,
                p.name AS productName,
                od.price AS price,
                od.quantity AS quantity,

                im.img_name AS img

            FROM orders o
            JOIN users u
                ON u.id = o.user_id

            LEFT JOIN carts c
                ON c.user_id = u.id

            JOIN order_details od
                ON od.order_id = o.id

            JOIN products p
                ON p.id = od.product_id

            LEFT JOIN product_image im
                ON im.product_id = p.id

            WHERE u.id = :userId

            ORDER BY o.order_date DESC, o.id DESC
            """, nativeQuery = true)
    List<OrderHistoryProjection> findOrderHistoryByUserId(
            @Param("userId") Long userId);

    @Query("""
            SELECT COALESCE(SUM(o.totalPrice), 0)
            FROM Order o
            WHERE o.orderDate >= :startDate
              AND o.orderDate < :endDate
              AND o.paymentStatus = :paymentStatus
              AND o.orderStatus <> :cancelledStatus
            """)

    BigDecimal calculateTotalRevenue(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("cancelledStatus") OrderStatus cancelledStatus);

    Optional<Order> findByOrderCode(Long orderCode);
}
