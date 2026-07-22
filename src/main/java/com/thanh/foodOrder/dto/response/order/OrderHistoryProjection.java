package com.thanh.foodorder.dto.response.order;

import java.time.Instant;

public interface OrderHistoryProjection {
    Long getUserId();

    String getFullName();

    Long getCartId();

    Long getOrderId();

    Instant getOrderDate();

    String getOrderStatus();

    Long getTableId();

    Double getTotalPrice();

    Long getProductId();

    String getProductName();

    Double getPrice();

    Long getQuantity();

    String getImg();
}