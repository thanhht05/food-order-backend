package com.thanh.foodorder.dto.response.order;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemDTO {
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;

}
