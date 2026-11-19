package com.thanh.foodorder.core.util.exception;

import lombok.Getter;

@Getter
public class CommonException extends RuntimeException {
    private final ErrorCode errorCode;

    public CommonException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public CommonException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CommonException(String message) {
        super(message);
        this.errorCode = ErrorCode.BUSINESS_LOGIC;
    }
}
