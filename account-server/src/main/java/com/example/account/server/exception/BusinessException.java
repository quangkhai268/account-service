package com.example.account.server.exception;

/** Lỗi nghiệp vụ / validate, được map sang tranStatus=1 + errordetails. */
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
