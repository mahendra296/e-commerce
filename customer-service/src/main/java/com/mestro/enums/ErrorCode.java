package com.mestro.enums;

public enum ErrorCode {
    CUSTOMER_NOT_FOUND("CUST_001", "Customer not found"),
    CUSTOMER_ADDRESS_NOT_FOUND("ADDR_001", "Customer address not found"),
    INVALID_INPUT("VAL_001", "Invalid input data"),
    DUPLICATE_EMAIL("CUST_002", "Email already exists"),
    INTERNAL_SERVER_ERROR("SYS_001", "Internal server error"),
    BAD_REQUEST("REQ_001", "Bad request");

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
