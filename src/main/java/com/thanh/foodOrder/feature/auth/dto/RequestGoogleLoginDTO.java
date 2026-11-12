package com.thanh.foodorder.feature.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestGoogleLoginDTO {
    @NotBlank(message = "Google ID Token cannot be empty")
    private String idToken;
}
