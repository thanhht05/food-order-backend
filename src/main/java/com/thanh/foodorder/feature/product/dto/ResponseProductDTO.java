package com.thanh.foodorder.feature.product.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.thanh.foodorder.feature.product.domain.ProductImage;
import com.thanh.foodorder.feature.product.enums.ProductStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResponseProductDTO {
    private long id;
    private String name;
    private BigDecimal price;
    private List<ProductImage> lstImg;
    private int quantity;
    private int sold;
    private String description;
    private ProductCate productCate;
    private Instant createdAt;
    private Instant updatedAt;
    private ProductStatus productStatus;

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductCate {
        private long id;
        private String name;
    }

    @Getter
    @Setter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ProductImage {
        private String name;
    }
}
