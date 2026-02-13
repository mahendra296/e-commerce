package com.mestro.enums;

import com.mestro.common.enums.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_FOUND("ORD_001", "Order not found"),
    ORDER_ALREADY_EXISTS("ORD_002", "Order already exists"),
    ORDER_CANNOT_BE_UPDATED("ORD_003", "Order cannot be updated"),
    ORDER_CANNOT_BE_DELETED("ORD_004", "Order cannot be deleted"),
    INVALID_ORDER_STATUS("ORD_005", "Invalid order status"),
    ORDER_ITEM_NOT_FOUND("ORI_001", "Order item not found"),
    INVALID_QUANTITY("ORI_002", "Invalid quantity"),
    INVALID_PRICE("ORI_003", "Invalid price"),
    CUSTOMER_NOT_FOUND("CUS_001", "Customer not found");

    private final String code;
    private final String message;
}
