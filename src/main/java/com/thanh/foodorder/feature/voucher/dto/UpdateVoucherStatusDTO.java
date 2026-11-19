package com.thanh.foodorder.feature.voucher.dto;

import com.thanh.foodorder.feature.voucher.enums.VoucherStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateVoucherStatusDTO {
    private VoucherStatus status;
}
