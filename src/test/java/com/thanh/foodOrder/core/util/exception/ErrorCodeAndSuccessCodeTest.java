package com.thanh.foodorder.core.util.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;

import com.thanh.foodorder.core.response.RestResponse;
import com.thanh.foodorder.core.util.annotation.SuccessCode;

class ErrorCodeAndSuccessCodeTest {

    private final GlobalException globalException = new GlobalException();

    @Test
    @DisplayName("CommonException(ErrorCode) trả về đúng mã ERR_ trong GlobalException")
    void handleCommonException_WithEnum() {
        CommonException ex = new CommonException(ErrorCode.VOUCHER_EXPIRED);
        ResponseEntity<RestResponse<Object>> response = globalException.handleCommonException(ex);

        assertEquals(400, response.getBody().getStatusCode());
        assertEquals("ERR_VOUCHER_EXPIRED", response.getBody().getError());
        assertEquals("Voucher is expired", response.getBody().getMessage());
    }

    @Test
    @DisplayName("CommonException(String) gán mặc định ERR_BUSINESS_LOGIC")
    void handleCommonException_WithLegacyString() {
        CommonException ex = new CommonException("Email test@gmail.com already exists");
        ResponseEntity<RestResponse<Object>> response = globalException.handleCommonException(ex);

        assertEquals(400, response.getBody().getStatusCode());
        assertEquals("ERR_BUSINESS_LOGIC", response.getBody().getError());
        assertEquals("Email test@gmail.com already exists", response.getBody().getMessage());
    }

    @Test
    @DisplayName("BadCredentialsException trả về mã ERR_BAD_CREDENTIALS")
    void handleBadCredentialsException_ReturnsErrorCode() {
        ResponseEntity<RestResponse<Object>> response = globalException
                .handleBadCredentialsException(new BadCredentialsException("Bad credentials"));

        assertEquals(400, response.getBody().getStatusCode());
        assertEquals("ERR_BAD_CREDENTIALS", response.getBody().getError());
        assertEquals("Username or password incorrect", response.getBody().getMessage());
    }

    @Test
    @DisplayName("SuccessCode.fromMessage ánh xạ chính xác các @ApiMessage hiện có sang mã SUC_")
    void successCode_FromMessage() {
        assertEquals(SuccessCode.LOGIN, SuccessCode.fromMessage("Login "));
        assertEquals(SuccessCode.GOOGLE_LOGIN, SuccessCode.fromMessage("Google Login"));
        assertEquals(SuccessCode.VOUCHER_UPDATE_STATUS, SuccessCode.fromMessage("Update voucher status"));
        assertEquals(SuccessCode.USER_CREATE, SuccessCode.fromMessage("Create a user"));
        assertEquals(SuccessCode.CALL_API_SUCCESS, SuccessCode.fromMessage("Unknown message"));
    }
}
