package com.thanh.foodorder.feature.order.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentConfirmRequest {
    private Long orderId;

    private BigDecimal amount;

}
