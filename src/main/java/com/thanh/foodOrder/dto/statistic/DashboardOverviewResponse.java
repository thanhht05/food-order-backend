package com.thanh.foodorder.dto.statistic;

import java.math.BigDecimal;

/**
 * DashboardOverviewResponse
 */
public record DashboardOverviewResponse(
        BigDecimal totalRevenue,
        Long totalOrders,
        Long completedOrders,
        Long cancelledOrders,
        Long totalProductsSold,
        Long totalCustomers,
        BigDecimal averageOrderValue) {
}