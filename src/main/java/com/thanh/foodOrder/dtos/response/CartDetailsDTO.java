package com.thanh.foodOrder.dtos.response;

import java.util.List;
import java.util.Locale.Category;

import com.thanh.foodOrder.domain.ProductImage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartDetailsDTO {

    // private Long id;
    private int quantity;
    private Double price;
    private Double totalPrice;
    // private Long userId;
    // private Long cartId;
    private ProductInnerCartDetail productInnerCartDetail;
    // private CategoryInnerCartDetail CategoryInnerCartDetail;

    @Getter
    @Setter
    public static class ProductInnerCartDetail {
        private Long id;
        private String name;
        private Double price;
        private List<ProductImage> lstImg;

    }

    // @Getter
    // @Setter
    // public static class CategoryInnerCartDetail {
    // private Long id;
    // private String name;

    // }

}