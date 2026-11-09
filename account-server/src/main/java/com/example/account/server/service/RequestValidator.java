package com.example.account.server.service;

import com.example.account.server.dto.GetAccountListRequest;
import com.example.account.server.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Validate input theo đặc tả. */
@Component
public class RequestValidator {

    /** Cả cifNo và idNo đều trống. */
    public static final String VAL001 = "VAL001";
    /** Sai định dạng / thiếu trường bắt buộc / vượt độ dài (mã bổ sung, đặc tả chưa quy định). */
    public static final String VAL002 = "VAL002";

    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public void validate(GetAccountListRequest req) {
        requireText("requestId", req.getRequestId(), 50);
        requireText("processTime", req.getProcessTime(), 30);
        requireText("appCode", req.getAppCode(), 20);
        checkMaxLength("cifNo", req.getCifNo(), 20);
        checkMaxLength("idNo", req.getIdNo(), 40);

        try {
            LocalDateTime.parse(req.getProcessTime().trim(), TIME_FORMAT);
        } catch (DateTimeParseException e) {
            throw new BusinessException(VAL002, "processTime phải có định dạng yyyyMMddHHmmss");
        }

        if (isBlank(req.getCifNo()) && isBlank(req.getIdNo())) {
            throw new BusinessException(VAL001, "Bắt buộc phải có cifNo hoặc idNo");
        }
    }

    private void requireText(String field, String value, int maxLength) {
        if (isBlank(value)) {
            throw new BusinessException(VAL002, field + " là bắt buộc");
        }
        checkMaxLength(field, value, maxLength);
    }

    private void checkMaxLength(String field, String value, int maxLength) {
        if (value != null && value.length() > maxLength) {
            throw new BusinessException(VAL002, field + " vượt quá độ dài tối đa " + maxLength);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
