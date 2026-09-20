package com.thanh.foodorder.feature.product.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDTO {

    private String name;
    private BigDecimal price;
    private Integer quantity;
    private int sold;
    private List<String> lstImg;
    private String description;
    private String categoryName;
}
