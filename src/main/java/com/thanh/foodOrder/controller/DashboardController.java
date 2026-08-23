package com.thanh.foodorder.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.thanh.foodorder.dto.statistic.OrderStatusStatistic;
import com.thanh.foodorder.service.DashboardService;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

}
