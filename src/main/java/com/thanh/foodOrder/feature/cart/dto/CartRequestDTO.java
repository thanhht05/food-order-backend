package com.thanh.foodorder.feature.cart.dto;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequestDTO {
    private Long productId;
    private Integer quantity;
}
