package com.thanh.foodorder.core.util.annotation;

/**
 * Tập trung toàn bộ mã thành công (Success Code) của API về một nơi duy nhất.
 * Được trả về kèm trong RestResponse để Frontend (axios interceptor) tự động dịch
 * sang ngôn ngữ hiện tại (vie.json / en.json).
 */
public enum SuccessCode {
    CALL_API_SUCCESS("SUC_CALL_API", "Call api success"),

    // Auth
    LOGIN("SUC_LOGIN", "Login "),
    GOOGLE_LOGIN("SUC_GOOGLE_LOGIN", "Google Login"),
    GET_USER_ACCOUNT("SUC_GET_ACCOUNT", "Get user account"),
    LOGOUT("SUC_LOGOUT", "Logout successful"),
    CHANGE_PASSWORD("SUC_CHANGE_PASSWORD", "Change password successfully"),
    REGISTER("SUC_REGISTER", "Register successfully"),

    // User
    USER_CREATE("SUC_USER_CREATE", "Create a user"),
    USER_UPDATE("SUC_USER_UPDATE", "Update a user"),
    USER_DELETE("SUC_USER_DELETE", "Delete a user"),
    USER_GET_BY_ID("SUC_USER_GET_BY_ID", "Get user by id"),
    USER_GET_ALL("SUC_USER_GET_ALL", "Get all users"),
    USER_BULK_CREATE("SUC_USER_BULK_CREATE", "Create users in bulk"),

    // Role
    ROLE_CREATE("SUC_ROLE_CREATE", "Create a new role"),
    ROLE_UPDATE("SUC_ROLE_UPDATE", "Update a role"),
    ROLE_DELETE("SUC_ROLE_DELETE", "Delete a role"),

    // Category
    CATEGORY_CREATE("SUC_CATEGORY_CREATE", "Create a category"),
    CATEGORY_UPDATE("SUC_CATEGORY_UPDATE", "Update a category "),
    CATEGORY_DELETE("SUC_CATEGORY_DELETE", "Delete a category"),

    // Product
    PRODUCT_CREATE("SUC_PRODUCT_CREATE", "Create a product"),
    PRODUCT_UPDATE("SUC_PRODUCT_UPDATE", "Update a product"),
    PRODUCT_DELETE("SUC_PRODUCT_DELETE", "Delete a product"),

    // Voucher
    VOUCHER_CREATE("SUC_VOUCHER_CREATE", "Create a voucher"),
    VOUCHER_UPDATE("SUC_VOUCHER_UPDATE", "Update a voucher "),
    VOUCHER_UPDATE_STATUS("SUC_VOUCHER_UPDATE_STATUS", "Update voucher status"),
    VOUCHER_DELETE("SUC_VOUCHER_DELETE", "Delete a voucher"),

    // Order & Cart
    ORDER_PLACE("SUC_ORDER_PLACE", "Place order successfully"),
    ORDER_UPDATE("SUC_ORDER_UPDATE", "Update order successfully");

    private final String code;
    private final String defaultMessage;

    SuccessCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public static SuccessCode fromMessage(String message) {
        if (message == null || message.isBlank()) {
            return CALL_API_SUCCESS;
        }
        String trimmed = message.trim().toLowerCase();
        for (SuccessCode sc : values()) {
            if (sc.defaultMessage.trim().equalsIgnoreCase(trimmed) || sc.code.equalsIgnoreCase(trimmed)) {
                return sc;
            }
        }
        return CALL_API_SUCCESS;
    }
}
