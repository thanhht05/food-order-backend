package com.thanh.foodorder.feature.order.dto;

import java.util.List;


import lombok.Getter;
import lombok.Setter;
import com.thanh.foodorder.feature.user.domain.Address;

@Getter
@Setter
public class CheckoutRequestDTO {
    private List<Long> cartDetailIds; // các dòng trong giỏ
    private String voucherCode;
    private String note;
    private String paymentMethod;
    private Address shippingAddress;
}
