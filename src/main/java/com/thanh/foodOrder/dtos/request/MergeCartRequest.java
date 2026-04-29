package com.thanh.foodOrder.dtos.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MergeCartRequest {
    private long userId;
    private List<CartItemRequestDTO> items;

}
