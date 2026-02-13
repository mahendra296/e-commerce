package com.mestro.enums;

import com.mestro.common.enums.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CustomerErrorCode implements ErrorCode {
    CUSTOMER_NOT_FOUND("CUST_001", "Customer not found"),
    CUSTOMER_ADDRESS_NOT_FOUND("ADDR_001", "Customer address not found"),
    DUPLICATE_EMAIL("CUST_002", "Email already exists");

    private final String code;
    private final String message;
}
