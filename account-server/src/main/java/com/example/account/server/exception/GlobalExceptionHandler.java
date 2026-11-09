package com.example.account.server.exception;

import com.example.account.server.dto.GetAccountListResponse;
import com.example.account.server.service.AccountRequestProcessor;
import com.example.account.server.service.ResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Lưới an toàn cho lỗi phát sinh ngoài AccountRequestProcessor (ví dụ không ghi được log
 * lúc nhận request). Vẫn trả đúng cấu trúc response của đặc tả.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public GetAccountListResponse handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseFactory.fail(null, AccountRequestProcessor.SYS999, "Lỗi hệ thống");
    }
}
