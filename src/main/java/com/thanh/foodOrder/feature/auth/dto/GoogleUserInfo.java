package com.thanh.foodorder.feature.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class GoogleUserInfo {
    private String sub;
    private String email;
    private String name;
    private String picture;
    private boolean emailVerified;
}
