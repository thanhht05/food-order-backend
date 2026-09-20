package com.thanh.foodorder.feature.order.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BuyNowRequestDTO {
    private long productId;
    private int quantity;
    private double price;
    private String voucherCode;
    private String note;
    private String paymentMethod;
    private Long tableId;

}
