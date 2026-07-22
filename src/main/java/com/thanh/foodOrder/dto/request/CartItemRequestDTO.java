package com.thanh.foodorder.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemRequestDTO {
    private Long productId;
    private Integer quantity;

}
