package com.thanh.foodOrder.domain.respone.product;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ResponseProductDTO {
    private long id;
    private String name;
    private double price;
    private List<ProductImage> lstImg;
    private int quantity;
    private String description;
    private ProductCate productCate;
    private Instant createdAt;
    private Instant updatedAt;

    @Getter
    @Setter
    public static class ProductCate {
        private long id;
        private String name;
    }

    @Getter
    @Setter
    public static class ProductImage {
        private String name;
    }
}
