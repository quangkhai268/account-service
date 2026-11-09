package com.example.account.client.service;

import com.example.account.client.dto.GetAccountListRequest;
import com.example.account.client.entity.ApiCallLog;
import com.example.account.client.repository.ApiCallLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Ghi log DB. Dùng REQUIRES_NEW để mỗi thao tác commit ngay, nhờ vậy log PENDING
 * vẫn tồn tại ngay cả khi bước sau (gọi API / insert dữ liệu) lỗi hoặc rollback.
 */
@Service
public class ApiLogService {

    private final ApiCallLogRepository repository;

    public ApiLogService(ApiCallLogRepository repository) {
        this.repository = repository;
    }

    /** Ghi log trước khi gọi API (status = PENDING). Trả về id của dòng log. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long start(GetAccountListRequest req, String endpoint, String requestBody) {
        ApiCallLog log = new ApiCallLog();
        log.setRequestId(req.getRequestId());
        log.setAppCode(req.getAppCode());
        log.setEndpoint(endpoint);
        log.setRequestTime(LocalDateTime.now());
        log.setRequestBody(requestBody);
        log.setStatus(ApiCallLog.PENDING);
        return repository.save(log).getId();
    }

    /** Cập nhật log khi không có response hợp lệ (timeout, lỗi mạng, HTTP lỗi, lỗi lưu DB...). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void fail(Long logId, String status, Integer httpStatus, String responseBody,
                     String tranStatus, String tranDesc, String errorCode, long durationMs) {
        ApiCallLog log = repository.findById(logId).orElseThrow();
        apply(log, status, httpStatus, responseBody, tranStatus, tranDesc, errorCode, durationMs);
        repository.save(log);
    }

    /** Dùng chung cho cả nhánh thành công (được gọi trong transaction của AccountPersistenceService). */
    public void apply(ApiCallLog log, String status, Integer httpStatus, String responseBody,
                      String tranStatus, String tranDesc, String errorCode, long durationMs) {
        log.setResponseTime(LocalDateTime.now());
        log.setStatus(status);
        log.setHttpStatus(httpStatus);
        log.setResponseBody(responseBody);
        log.setTranStatus(tranStatus);
        log.setTranDesc(trunc(tranDesc, 200));
        log.setErrorCode(trunc(errorCode, 100));
        log.setDurationMs(durationMs);
    }

    static String trunc(String s, int max) {
        return (s == null || s.length() <= max) ? s : s.substring(0, max);
    }
}
