package com.thanh.foodOrder.dtos.response.order;

import java.time.LocalDateTime;
import java.util.List;

import com.thanh.foodOrder.enums.PaymentStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderResponseDTO {

    private Long orderId;
    private LocalDateTime orderDate;
    private String status;

    private Double totalPrice;
    private Double discount;
    private Long tableId;
    private PaymentStatus paymentStatus;

    private List<OrderItemDTO> items;
}
