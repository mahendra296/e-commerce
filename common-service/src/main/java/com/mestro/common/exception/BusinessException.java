package com.mestro.common.exception;

import com.mestro.common.enums.CommonErrorCode;
import com.mestro.common.enums.ErrorCode;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;

    public BusinessException(String message) {
        super(message);
        this.errorCode = CommonErrorCode.BAD_REQUEST;
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}
