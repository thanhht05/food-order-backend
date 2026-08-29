package com.thanh.foodorder.dto.response.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.thanh.foodorder.enums.PaymentStatus;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderResponseDTO {

    private Long orderId;
    private LocalDateTime orderDate;
    private String status;

    private BigDecimal totalPrice;
    private BigDecimal discount;
    private PaymentStatus paymentStatus;

    private List<OrderItemDTO> items;
}
