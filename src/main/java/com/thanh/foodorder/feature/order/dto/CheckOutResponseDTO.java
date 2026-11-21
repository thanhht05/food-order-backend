package com.thanh.foodorder.feature.order.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckOutResponseDTO {
    private List<Long> cartDetailIds = new ArrayList<>(); // cart item IDs
    private BigDecimal totalPrice;
    private BigDecimal discount;
    private BigDecimal finalPrice;
    private Long tableId;
}
