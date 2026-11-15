package com.thanh.foodorder.core.util.exception;

/**
 * Tập trung toàn bộ mã lỗi của hệ thống vào một nơi duy nhất (theo mẫu AuthErrorCode).
 * Trường `code` được trả về trong `RestResponse.error` để Frontend (axios interceptor)
 * tự động dịch sang ngôn ngữ hiện tại (vie.json / en.json) mà không cần sửa từng trang.
 */
public enum ErrorCode {
    // 1. System & Common
    INTERNAL_SERVER("ERR_INTERNAL_SERVER", "Something went wrong"),
    BUSINESS_LOGIC("ERR_BUSINESS_LOGIC", "Business logic error"),
    VALIDATION_ERROR("ERR_VALIDATION", "Validation error"),
    ACCESS_DENIED("ERR_ACCESS_DENIED", "Access denied: You do not have permission to access this resource"),
    UPLOAD_IMAGE_FAILED("ERR_UPLOAD_IMAGE_FAILED", "Upload image failed"),

    // 2. Auth & Session
    BAD_CREDENTIALS("ERR_BAD_CREDENTIALS", "Username or password incorrect"),
    LOGGED_IN_ELSEWHERE("ERR_LOGGED_IN_ELSEWHERE",
            "Your account was just logged in on another browser."),
    PASSWORD_CHANGED("ERR_PASSWORD_CHANGED",
            "Password has been changed. Please log in again."),
    COOKIE_NOT_FOUND("ERR_COOKIE_NOT_FOUND", "Cookie does not exist"),
    INVALID_REFRESH_TOKEN("ERR_INVALID_REFRESH_TOKEN", "Invalid refresh token"),
    REFRESH_TOKEN_REVOKED("ERR_REFRESH_TOKEN_REVOKED", "User not found or refresh token revoked"),
    REFRESH_TOKEN_EXPIRED("ERR_REFRESH_TOKEN_EXPIRED", "Refresh token invalid or expired"),
    GOOGLE_TOKEN_INVALID("ERR_GOOGLE_TOKEN_INVALID",
            "Google ID Token verification failed or token has expired"),

    // 3. User & Role
    USER_NOT_FOUND("ERR_USER_NOT_FOUND", "User not found"),
    EMAIL_ALREADY_EXISTS("ERR_EMAIL_ALREADY_EXISTS", "Email already exists"),
    OLD_PASSWORD_INCORRECT("ERR_OLD_PASSWORD_INCORRECT", "Old password incorrect"),
    CONFIRM_PASSWORD_INCORRECT("ERR_CONFIRM_PASSWORD_INCORRECT", "Confirm password incorrect"),
    ROLE_NOT_FOUND("ERR_ROLE_NOT_FOUND", "Role not found"),
    ROLE_ALREADY_EXISTS("ERR_ROLE_ALREADY_EXISTS", "Role already exists"),

    // 4. Product & Category
    PRODUCT_NOT_FOUND("ERR_PRODUCT_NOT_FOUND", "Product not found"),
    PRODUCT_ALREADY_EXISTS("ERR_PRODUCT_ALREADY_EXISTS", "Product already exists in this category"),
    PRODUCT_OUT_OF_STOCK("ERR_PRODUCT_OUT_OF_STOCK", "Product does not have enough stock"),
    INVALID_SOLD_QUANTITY("ERR_INVALID_SOLD_QUANTITY", "Sold quantity must be >= 0"),
    CATEGORY_NOT_FOUND("ERR_CATEGORY_NOT_FOUND", "Category not found"),
    CATEGORY_ALREADY_EXISTS("ERR_CATEGORY_ALREADY_EXISTS", "Category name already exists"),

    // 5. Cart & Order
    CART_NOT_FOUND("ERR_CART_NOT_FOUND", "Cart not found"),
    CART_EMPTY("ERR_CART_EMPTY", "Cart is empty"),
    NO_AVAILABLE_ITEMS("ERR_NO_AVAILABLE_ITEMS", "No available items to place order"),
    CART_ITEM_UNAUTHORIZED("ERR_CART_ITEM_UNAUTHORIZED", "CartDetail does not belong to the current user"),
    INVALID_ITEM_QUANTITY("ERR_INVALID_ITEM_QUANTITY", "Item quantity is invalid"),
    ORDER_NOT_FOUND("ERR_ORDER_NOT_FOUND", "Order not found"),
    ORDER_ALREADY_PAID("ERR_ORDER_ALREADY_PAID", "Order already paid"),
    ORDER_PRICE_INVALID("ERR_ORDER_PRICE_INVALID", "Price invalid"),

    // 6. Voucher
    VOUCHER_NOT_FOUND("ERR_VOUCHER_NOT_FOUND", "Voucher not found"),
    VOUCHER_ALREADY_EXISTS("ERR_VOUCHER_ALREADY_EXISTS", "Voucher already exists"),
    VOUCHER_EXPIRATION_PAST("ERR_VOUCHER_EXPIRATION_PAST", "Expiration date must be in the future"),
    VOUCHER_INACTIVE("ERR_VOUCHER_INACTIVE", "Voucher is inactive"),
    VOUCHER_EXPIRED("ERR_VOUCHER_EXPIRED", "Voucher is expired"),
    VOUCHER_FULLY_USED("ERR_VOUCHER_FULLY_USED", "Voucher has been fully used."),
    VOUCHER_ALREADY_USED_BY_USER("ERR_VOUCHER_ALREADY_USED_BY_USER",
            "Voucher has already been used by this user."),

    // 7. Chat & AI
    CONVERSATION_NOT_FOUND("ERR_CONVERSATION_NOT_FOUND", "Conversation not found"),
    AI_PARSE_FAILED("ERR_AI_PARSE_FAILED", "Failed to parse user request");

    private final String code;
    private final String message;

    ErrorCode(String code, String message) {
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
