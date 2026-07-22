package com.thanh.foodorder.dto.response.order;

import java.time.LocalDateTime;

import com.thanh.foodorder.domain.Order;
import com.thanh.foodorder.enums.OrderStatus;
import com.thanh.foodorder.enums.PaymentStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AdminOrderResponseDTO {
    private Long orderId;
    private LocalDateTime orderDate;
    private OrderStatus status;

    private Double totalPrice;

    private Long tableId;
    private PaymentStatus paymentStatus;

    public static AdminOrderResponseDTO from(Order order) {
        return AdminOrderResponseDTO.builder()
                .orderId(order.getId())
                .orderDate(order.getOrderDate())
                .status(order.getOrderStatus())
                .totalPrice(order.getTotalPrice())
                .tableId(order.getBookingTable().getId())
                .paymentStatus(order.getPaymentStatus())
                .build();

    }

}
