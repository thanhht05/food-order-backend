package com.thanh.foodorder.feature.cart.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MergeCartRequest {
    private long userId;
    private List<CartItemRequestDTO> items;

}
