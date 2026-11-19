package com.thanh.foodorder.feature.dashboard.service;

import org.springframework.stereotype.Service;
import com.thanh.foodorder.feature.order.repository.OrderRepository;


@Service
public class StatisticService {
    private final OrderRepository orderRepository;

    public StatisticService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
