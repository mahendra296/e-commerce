package com.mestro.common.exception;

import com.mestro.common.enums.CommonErrorCode;
import com.mestro.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class ResourceAlreadyExistsException extends RuntimeException {
    private final ErrorCode errorCode;

    public ResourceAlreadyExistsException(String message) {
        super(message);
        this.errorCode = CommonErrorCode.RESOURCE_ALREADY_EXISTS;
    }

    public ResourceAlreadyExistsException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ResourceAlreadyExistsException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
        this.errorCode = CommonErrorCode.RESOURCE_ALREADY_EXISTS;
    }
}
