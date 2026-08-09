package com.thanh.foodorder.dto.request;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentConfirmRequest {
    private Long orderId;

    private BigDecimal amount;

}
