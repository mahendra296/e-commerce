package com.mestro.common.exception;

import com.mestro.common.enums.CommonErrorCode;
import com.mestro.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {
    private final ErrorCode errorCode;

    public DuplicateResourceException(String message) {
        super(message);
        this.errorCode = CommonErrorCode.DUPLICATE_RESOURCE;
    }

    public DuplicateResourceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue));
        this.errorCode = CommonErrorCode.DUPLICATE_RESOURCE;
    }
}
