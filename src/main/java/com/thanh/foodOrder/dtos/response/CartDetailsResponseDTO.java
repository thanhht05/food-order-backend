package com.thanh.foodOrder.dtos.response;

import java.time.Instant;
import java.util.List;
import java.util.Locale.Category;

import com.thanh.foodOrder.domain.ProductImage;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartDetailsResponseDTO {

    private int quantity;
    private Double totalPrice;

    private List<ProductInnerCartDetail> productsInnerCartDetail;

    @Getter
    @Setter
    public static class ProductInnerCartDetail {
        private Long id;
        private String name;
        private Double price;
        private String categoryName;
        private List<ProductImage> lstImg;

    }

}