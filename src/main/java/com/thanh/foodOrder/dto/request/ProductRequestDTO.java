package com.thanh.foodorder.dto.request;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductRequestDTO {

    private String name;
    private Double price;
    private Integer quantity;
    private int sold;
    private List<String> lstImg;
    private String description;
    private String categoryName;
}
