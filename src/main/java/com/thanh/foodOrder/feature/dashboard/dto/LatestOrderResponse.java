package com.thanh.foodorder.feature.dashboard.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import com.thanh.foodorder.feature.order.enums.OrderStatus;


public record LatestOrderResponse(Long id,
        String fullName,
        BigDecimal totalAmount,
        Instant createdAt,
        OrderStatus orderStatus) {

}
