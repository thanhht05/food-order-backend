package com.thanh.foodorder.feature.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestRegisterDTO {
    private String email;
    private String fullName;
    private String phone;
    private String password;
}
