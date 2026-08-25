package com.thanh.foodorder.dto.statistic;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import com.thanh.foodorder.enums.OrderStatus;

public record LatestOrderResponse(Long id,
        String fullName,
        BigDecimal totalAmount,
        Instant createdAt,
        OrderStatus orderStatus) {

}
