package com.thanh.foodorder.feature.order.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CreatePaymentLinkRequestBody {
    private String productName;
    private String description;
    private String returnUrl;
    private BigDecimal price;
    private String cancelUrl;

}