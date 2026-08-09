package com.thanh.foodorder.service;

import org.springframework.stereotype.Service;

import com.thanh.foodorder.repository.OrderRepository;

@Service
public class StatisticService {
    private final OrderRepository orderRepository;

    public StatisticService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
}
