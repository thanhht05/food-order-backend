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

    private String province; // e.g. Thua Thien Hue
    private String ward; // e.g. Thuan Hoa
    private String addressDetail; // e.g. 199 Dien Bien Phu

}
