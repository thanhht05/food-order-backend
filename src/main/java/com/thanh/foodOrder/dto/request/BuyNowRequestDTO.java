package com.thanh.foodorder.dto.request;

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
