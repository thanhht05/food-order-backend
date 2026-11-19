package com.thanh.foodorder.feature.order.dto;

import java.time.Instant;

public interface OrderHistoryProjection {
    Long getUserId();

    String getFullName();

    Long getCartId();

    Long getOrderId();

    Instant getOrderDate();

    String getOrderStatus();

    String getPaymentMethod();

    String getPaymentStatus();

    Double getTotalPrice();

    Long getProductId();

    String getProductName();

    Double getPrice();

    Long getQuantity();

    String getImg();

    String getRecipientName();

    String getPhone();

    String getProvince();

    String getWard();

    String getAddressDetail();

    String getPaymentLinkId();
}