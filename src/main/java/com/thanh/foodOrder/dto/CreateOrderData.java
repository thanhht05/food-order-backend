package com.thanh.foodorder.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.thanh.foodorder.domain.Address;
import com.thanh.foodorder.domain.User;
import com.thanh.foodorder.domain.Voucher;
import com.thanh.foodorder.enums.OrderStatus;
import com.thanh.foodorder.enums.PaymentStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

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