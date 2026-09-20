package com.thanh.foodorder.feature.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import com.thanh.foodorder.feature.order.enums.PaymentStatus;
import com.thanh.foodorder.feature.user.domain.Address;

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
