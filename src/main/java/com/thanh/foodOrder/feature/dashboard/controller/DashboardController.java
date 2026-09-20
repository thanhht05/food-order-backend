package com.thanh.foodorder.feature.dashboard.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.thanh.foodorder.feature.dashboard.dto.DashboardOverviewResponse;
import com.thanh.foodorder.feature.dashboard.dto.LatestOrderResponse;
import com.thanh.foodorder.feature.dashboard.dto.OrderStatusStatistic;
import com.thanh.foodorder.feature.dashboard.dto.RevenueByMonthResponse;
import com.thanh.foodorder.feature.dashboard.dto.TopProductResponse;
import com.thanh.foodorder.feature.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/v1")
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/count-orderStatus")
    public ResponseEntity<List<OrderStatusStatistic>> handleCountOrderStatus() {
        return ResponseEntity.status(HttpStatus.OK).body(this.dashboardService.countOrderStatus());
    }

    @GetMapping("/statistic-overview")
    public ResponseEntity<DashboardOverviewResponse> handleStatisticOverview() {
        return ResponseEntity.status(HttpStatus.OK).body(this.dashboardService.statistciOverview());
    }

    @GetMapping("/get-latest-order")
    public ResponseEntity<List<LatestOrderResponse>> getMethodName() {
        return ResponseEntity.status(HttpStatus.OK).body(this.dashboardService.getLatestOrder());
    }

    @GetMapping("/get-top-product")
    public ResponseEntity<List<TopProductResponse>> handleGetTopProduct() {
        return ResponseEntity.status(HttpStatus.OK).body(this.dashboardService.getTopProducts());
    }

    @GetMapping("/get-revenue-month")
    public ResponseEntity<List<RevenueByMonthResponse>> handleGetRevenueByMonth() {
        return ResponseEntity.status(HttpStatus.OK).body(this.dashboardService.getRevenueByMonth());
    }

}
