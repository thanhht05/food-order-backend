package com.thanh.foodorder.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.thanh.foodorder.domain.Order;
import com.thanh.foodorder.dto.statistic.OrderStatusStatistic;

@Repository
public interface DashboardRepository extends JpaRepository<Order, Long> {

    @Query("""
            SELECT new com.thanh.foodorder.dto.statistic.OrderStatusStatistic(o.orderStatus, COUNT(*))
            FROM Order o
            GROUP BY o.orderStatus

            """)
    List<OrderStatusStatistic> countByOrderStatus();

}
