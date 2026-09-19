package com.thanh.foodorder.dto.response.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.thanh.foodorder.domain.Address;
import com.thanh.foodorder.enums.PaymentStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderResponseDTO {
    private String customerName;
    private String email;

    private Long orderId;
    private LocalDateTime orderDate;
    private String status;

    private BigDecimal totalPrice;
    private BigDecimal discount;
    private PaymentStatus paymentStatus;

    private List<OrderItemDTO> items;

    private Address address;
}
