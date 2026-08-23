package com.thanh.foodorder.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.thanh.foodorder.dto.statistic.OrderStatusStatistic;
import com.thanh.foodorder.repository.DashboardRepository;

@Service
public class DashboardService {
    private final DashboardRepository dashboardRepository;

    public DashboardService(DashboardRepository dashboardRepository) {
        this.dashboardRepository = dashboardRepository;
    }

    public List<OrderStatusStatistic> countOrderStatus() {
        return this.dashboardRepository.countByOrderStatus();
    }
}
