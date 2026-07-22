package com.thanh.foodorder.domain;

import java.time.Instant;
import java.time.LocalDate;

import com.thanh.foodorder.util.JwtUtil;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "vouchers")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String code;
    private int percentDiscount;
    private int maxDiscount;

    private LocalDate expiration;
    private String createdBy;
    private String updatedBy;

    private Instant createdAt;
    private Instant updatedAt;
    private int usageLimit;

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
}
