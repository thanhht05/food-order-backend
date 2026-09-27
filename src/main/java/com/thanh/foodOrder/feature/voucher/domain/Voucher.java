package com.thanh.foodorder.feature.voucher.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import com.thanh.foodorder.core.util.JwtUtil;
import com.thanh.foodorder.feature.voucher.enums.VoucherStatus;

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
    private BigDecimal maxDiscount;

    @Enumerated(EnumType.STRING)
    private VoucherStatus status;

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
        if (this.status == null) {
            this.status = VoucherStatus.ACTIVE;
        }
    }

    @PreUpdate
    public void handleBeforeUpdated() {
        this.updatedBy = JwtUtil.getCurrentUserLogin().orElse("");
        this.updatedAt = Instant.now();
    }
}
