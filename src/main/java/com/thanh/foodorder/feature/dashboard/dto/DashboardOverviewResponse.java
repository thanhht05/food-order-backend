package com.thanh.foodorder.feature.dashboard.dto;

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