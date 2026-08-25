package com.thanh.foodorder.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.thanh.foodorder.domain.Order;
import com.thanh.foodorder.dto.statistic.DashboardOverviewResponse;
import com.thanh.foodorder.dto.statistic.LatestOrderResponse;
import com.thanh.foodorder.dto.statistic.OrderStatusStatistic;
import com.thanh.foodorder.dto.statistic.RevenueByMonthResponse;
import com.thanh.foodorder.dto.statistic.TopProductResponse;

@Repository
public interface DashboardRepository extends JpaRepository<Order, Long> {

        @Query("""
                        SELECT new com.thanh.foodorder.dto.statistic.OrderStatusStatistic(o.orderStatus, COUNT(o))
                        FROM Order o
                        GROUP BY o.orderStatus

                        """)
        List<OrderStatusStatistic> countByOrderStatus();

        @Query(value = """
                        SELECT
                            (SELECT COUNT(*)
                             FROM food_order.orders) AS total_order,

                            (SELECT COALESCE(SUM(total_price), 0)
                             FROM food_order.orders
                             WHERE order_status = 'CONFIRMED') AS total_revenue,

                            (SELECT COUNT(*)
                             FROM food_order.users) AS total_user,

                            (SELECT COUNT(*)
                             FROM food_order.orders
                             WHERE order_status = 'PENDING') AS pending_order
                        """, nativeQuery = true)
        DashboardOverviewResponse getStatisticOverview();

        @Query("""
                            SELECT new com.thanh.foodorder.dto.statistic.LatestOrderResponse(
                                o.id,
                                 u.fullName,
                                o.totalPrice,
                                o.createdAt,
                                o.orderStatus
                            )
                            FROM Order o
                            JOIN o.user u

                            ORDER BY o.createdAt DESC
                        """)
        List<LatestOrderResponse> findLatestdOrders(Pageable pageable);

        @Query("""
                            SELECT new com.thanh.foodorder.dto.statistic.TopProductResponse(
                                p.name,
                                p.sold,
                                 i.imgName

                            )
                            FROM Product p
                            JOIN p.lstImg i
                            WHERE i.is_primary=true

                            ORDER BY p.sold DESC
                        """)
        List<TopProductResponse> findTopProducts(Pageable pageable);

        @Query("""
                            SELECT new com.thanh.foodorder.dto.statistic.RevenueByMonthResponse(
                                MONTH(o.createdAt),
                                SUM(o.totalPrice)
                            )
                            FROM Order o
                            WHERE o.orderStatus = 'CONFIRMED'
                              AND YEAR(o.createdAt) = YEAR(CURRENT_DATE)
                            GROUP BY MONTH(o.createdAt)
                            ORDER BY MONTH(o.createdAt)
                        """)
        List<RevenueByMonthResponse> getRevenueByMonth();
}
