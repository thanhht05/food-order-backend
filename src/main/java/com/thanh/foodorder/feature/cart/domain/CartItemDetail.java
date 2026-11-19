package com.thanh.foodorder.feature.cart.domain;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;
import com.thanh.foodorder.feature.product.domain.ProductImage;

@Getter
@Setter
public class CartItemDetail implements Serializable {

    private Long productId;
    private String productName;
    private double price;
    private int quantity;
    private List<ProductImage> lstImageUrl;

    @JsonIgnore
    public double getSubtotal() {
        return price * quantity;
    }
}
