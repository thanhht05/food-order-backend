package com.thanh.foodorder.feature.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import com.thanh.foodorder.feature.order.enums.OrderStatus;
import com.thanh.foodorder.feature.order.enums.PaymentStatus;
import com.thanh.foodorder.feature.user.domain.Address;
import com.thanh.foodorder.feature.user.domain.User;
import com.thanh.foodorder.feature.voucher.domain.Voucher;

@Getter
@Setter
@Builder
public class CreateOrderData {

    private User user;
    private LocalDateTime orderDate;
    private BigDecimal totalPrice;
    private BigDecimal discount;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
    private String paymentMethod;
    private Voucher voucher;
    private String note;
    private Address address;

}