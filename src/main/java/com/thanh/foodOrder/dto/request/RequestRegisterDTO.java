package com.thanh.foodorder.dto.request;

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
