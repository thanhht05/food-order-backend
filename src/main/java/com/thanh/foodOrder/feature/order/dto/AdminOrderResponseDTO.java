package com.thanh.foodorder.feature.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import com.thanh.foodorder.feature.order.domain.Order;
import com.thanh.foodorder.feature.order.enums.OrderStatus;
import com.thanh.foodorder.feature.order.enums.PaymentStatus;
import com.thanh.foodorder.feature.user.domain.Address;

@Getter
@Setter
@Builder
public class AdminOrderResponseDTO {
    private Long orderId;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private PaymentStatus paymentStatus;
    private InnerUserOrder innerUserOrder;

    public static AdminOrderResponseDTO from(Order order) {

        return AdminOrderResponseDTO.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .status(order.getOrderStatus())
                .totalPrice(order.getTotalPrice())
                .paymentStatus(order.getPaymentStatus())
                .innerUserOrder(
                        InnerUserOrder.builder()
                                .fullName(order.getUser().getFullName())
                                .email(order.getUser().getEmail())
                                .address(order.getAddress())
                                .build())
                .build();
    }

    @Getter
    @Setter
    @Builder
    public static class InnerUserOrder {
        private String fullName;
        private String email;

        private Address address;
    }

}
