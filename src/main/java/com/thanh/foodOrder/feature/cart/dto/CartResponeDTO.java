package com.thanh.foodorder.feature.cart.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartResponeDTO {
    private BigDecimal totalPrice;
    private long totalQuantity;
    private List<CartDetailUserDTO> lst;

}
