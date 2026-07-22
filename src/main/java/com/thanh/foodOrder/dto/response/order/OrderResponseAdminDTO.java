package com.thanh.foodorder.dto.response.order;

import java.time.Instant;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderResponseAdminDTO {
    private Long id;
    private LocalDateTime orderDate;
    private Double totalPrice;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
}
