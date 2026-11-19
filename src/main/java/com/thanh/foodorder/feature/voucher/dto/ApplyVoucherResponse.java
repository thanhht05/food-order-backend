package com.thanh.foodorder.feature.voucher.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ApplyVoucherResponse {
    private String code;

    private BigDecimal discountAmount;

    private BigDecimal originalTotal;

    private BigDecimal finalTotal;
}
