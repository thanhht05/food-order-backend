package com.thanh.foodorder.feature.order.repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.thanh.foodorder.feature.order.domain.Order;
import com.thanh.foodorder.feature.order.dto.OrderHistoryProjection;
import com.thanh.foodorder.feature.order.enums.OrderStatus;
import com.thanh.foodorder.feature.order.enums.PaymentStatus;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.voucher.domain.Voucher;


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
                o.payment_method as paymentMethod,
                o.payment_status as paymentStatus,

                o.total_price AS totalPrice,
                o.recipient_name as recipientName,
                o.phone as phone,
                o.province as province,
                o.ward as ward,
                o.address_detail as addressDetail,
                o.payment_link_id as paymentLinkId,



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

            WHERE u.id = :userId AND im.is_primary=1

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

    Optional<Order> findByPaymentLinkId(String id);
}
