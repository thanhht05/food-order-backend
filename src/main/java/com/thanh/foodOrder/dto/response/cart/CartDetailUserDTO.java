package com.thanh.foodorder.dto.response.cart;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartDetailUserDTO {
    private Long cartDetailId;
    private int quantity;
    private ProductInnerCartDetail productsInnerCartDetail;

    @Getter
    @Setter
    public static class ProductInnerCartDetail {
        private Long id;
        private String name;
        private Double price;
        private String categoryName;
        private String img;
        protected int quantity;

    }
}
