package com.mestro.enums;

public enum ErrorCode {
    // General errors
    INTERNAL_SERVER_ERROR("ERR_001", "Internal server error occurred"),
    VALIDATION_ERROR("ERR_002", "Validation error"),
    RESOURCE_NOT_FOUND("ERR_003", "Resource not found"),
    BAD_REQUEST("ERR_004", "Bad request"),

    // Order specific errors
    ORDER_NOT_FOUND("ORD_001", "Order not found"),
    ORDER_ALREADY_EXISTS("ORD_002", "Order already exists"),
    ORDER_CANNOT_BE_UPDATED("ORD_003", "Order cannot be updated"),
    ORDER_CANNOT_BE_DELETED("ORD_004", "Order cannot be deleted"),
    INVALID_ORDER_STATUS("ORD_005", "Invalid order status"),

    // Order Item specific errors
    ORDER_ITEM_NOT_FOUND("ORI_001", "Order item not found"),
    INVALID_QUANTITY("ORI_002", "Invalid quantity"),
    INVALID_PRICE("ORI_003", "Invalid price"),

    // Customer errors
    CUSTOMER_NOT_FOUND("CUS_001", "Customer not found");

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
