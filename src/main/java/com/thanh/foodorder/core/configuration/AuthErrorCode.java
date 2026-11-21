package com.thanh.foodorder.core.configuration;

/**
 * Authentication error code returned in the "error" field of 401 responses,
 * allowing the frontend to distinguish why a session was invalidated.
 */
public enum AuthErrorCode {
    /** Account logged in from another browser/device (single active session). */
    LOGGED_IN_ELSEWHERE("ERR_LOGGED_IN_ELSEWHERE",
            "Tài khoản của bạn vừa được đăng nhập ở một trình duyệt khác."),

    /** Password was changed (tokenVersion incremented). */
    PASSWORD_CHANGED("ERR_PASSWORD_CHANGED",
            "Mật khẩu đã được thay đổi. Vui lòng đăng nhập lại.");

    private final String code;
    private final String message;

    AuthErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}

