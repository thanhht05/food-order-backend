package com.thanh.foodorder.feature.user.domain;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Address {
    private String recipientName;
    private String phone;

    private String province; // Thành phố Huế
    private String ward; // Phường Thuận Hóa
    private String addressDetail; // 199 Điện Biên Phủ

}
