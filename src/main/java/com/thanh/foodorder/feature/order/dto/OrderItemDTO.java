package com.thanh.foodorder.feature.order.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderItemDTO {
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;

}
