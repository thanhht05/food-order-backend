package com.thanh.foodorder.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.thanh.foodorder.dto.statistic.DashboardOverviewResponse;
import com.thanh.foodorder.dto.statistic.LatestOrderResponse;
import com.thanh.foodorder.dto.statistic.OrderStatusStatistic;
import com.thanh.foodorder.dto.statistic.RevenueByMonthResponse;
import com.thanh.foodorder.dto.statistic.TopProductResponse;
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

    public DashboardOverviewResponse statistciOverview() {
        return this.dashboardRepository.getStatisticOverview();
    }

    public List<LatestOrderResponse> getLatestOrder() {
        return this.dashboardRepository.findLatestdOrders(PageRequest.of(0, 5));
    }

    public List<TopProductResponse> getTopProducts() {
        return this.dashboardRepository.findTopProducts(
                PageRequest.of(0, 5));
    }

    public List<RevenueByMonthResponse> getRevenueByMonth() {
        // 1. Khởi tạo Map 12 tháng với doanh thu mặc định là 0
        Map<Integer, BigDecimal> monthMap = new HashMap<>();
        for (int month = 1; month <= 12; month++) {
            monthMap.put(month, BigDecimal.ZERO);
        }

        // 2. Lấy dữ liệu từ DB và ghi đè vào Map
        List<RevenueByMonthResponse> dbData = dashboardRepository.getRevenueByMonth();
        for (RevenueByMonthResponse item : dbData) {
            monthMap.put(item.month(), item.revenue());
        }
        // 3. Chuyển Map thành List kết quả từ tháng 1 đến 12
        List<RevenueByMonthResponse> result = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            result.add(new RevenueByMonthResponse(month, monthMap.get(month)));
        }
        return result;

    }
}
