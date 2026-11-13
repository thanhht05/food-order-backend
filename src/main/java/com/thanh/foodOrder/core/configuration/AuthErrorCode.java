package com.thanh.foodorder.core.configuration;

/**
 * Mã lỗi xác thực trả về trong trường "error" của response 401,
 * để frontend phân biệt lý do phiên đăng nhập bị vô hiệu.
 */
public enum AuthErrorCode {
    /** Tài khoản đã đăng nhập ở trình duyệt/thiết bị khác (single active session). */
    LOGGED_IN_ELSEWHERE("ERR_LOGGED_IN_ELSEWHERE",
            "Tài khoản của bạn vừa được đăng nhập ở một trình duyệt khác."),

    /** Mật khẩu đã thay đổi (tokenVersion tăng). */
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

