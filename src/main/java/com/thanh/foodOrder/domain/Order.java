package com.thanh.foodorder.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.thanh.foodorder.enums.OrderStatus;
import com.thanh.foodorder.enums.PaymentStatus;
import com.thanh.foodorder.util.JwtUtil;

import jakarta.annotation.Generated;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime orderDate;
    private BigDecimal totalPrice;
    @Column(nullable = true)
    private BigDecimal discount;
    private String note;
    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;
    private String createdBy;
    private String updatedBy;
    private Instant createdAt;
    private Instant updatedAt;
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    @Embedded
    private Address address;

    private String paymentLinkId;
    private Long orderCode;

    @PrePersist
    public void handleBeforeCreated() {
        this.createdAt = Instant.now();
        this.createdBy = JwtUtil.getCurrentUserLogin().orElse("");
    }

    @PreUpdate
    public void handleBeforeUpdated() {
        this.updatedBy = JwtUtil.getCurrentUserLogin().orElse("");
        this.updatedAt = Instant.now();
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "voucher_id", nullable = true)

    private Voucher voucher;

    @OneToMany(mappedBy = "order")
    List<OrderDetail> orderDetails;

}
