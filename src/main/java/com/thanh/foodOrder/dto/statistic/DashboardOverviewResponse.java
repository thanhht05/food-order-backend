package com.thanh.foodorder.dto.statistic;

import java.math.BigDecimal;

/**
 * DashboardOverviewResponse
 */
public interface DashboardOverviewResponse {

        Long getTotalOrder();

        BigDecimal getTotalRevenue();

        Long getTotalUser();

        Long getPendingOrder();
}