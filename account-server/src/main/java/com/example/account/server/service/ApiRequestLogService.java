package com.example.account.server.service;

import com.example.account.server.dto.GetAccountListRequest;
import com.example.account.server.dto.GetAccountListResponse;
import com.example.account.server.entity.ApiRequestLog;
import com.example.account.server.repository.ApiRequestLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Ghi log request/response. Mọi thao tác ghi dùng REQUIRES_NEW nên commit độc lập:
 * request lỗi validate, request trùng, hoặc lỗi hệ thống vẫn được ghi log.
 */
@Service
public class ApiRequestLogService {

    private final ApiRequestLogRepository repository;

    public ApiRequestLogService(ApiRequestLogRepository repository) {
        this.repository = repository;
    }

    /** Khóa chống trùng, null nếu request thiếu appCode hoặc requestId. */
    public static String dedupKey(GetAccountListRequest req) {
        if (req == null || isBlank(req.getAppCode()) || isBlank(req.getRequestId())) {
            return null;
        }
        return req.getAppCode().trim() + "|" + req.getRequestId().trim();
    }

    /** Request này đã được nhận (và chấp nhận) trước đó chưa. */
    public boolean isDuplicate(GetAccountListRequest req) {
        String key = dedupKey(req);
        return key != null && repository.existsByDedupKey(key);
    }

    /**
     * Ghi log lúc nhận request. Nếu {@code duplicate} = false thì gán khóa chống trùng; khi hai request
     * trùng đến cùng lúc, unique index sẽ làm request sau ném DataIntegrityViolationException
     * (caller xử lý bằng cách ghi lại với duplicate = true).
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Long register(GetAccountListRequest req, String rawBody, boolean duplicate) {
        ApiRequestLog log = new ApiRequestLog();
        if (req != null) {
            log.setRequestId(trunc(req.getRequestId(), 50));
            log.setAppCode(trunc(req.getAppCode(), 20));
        }
        log.setDedupKey(duplicate ? null : trunc(dedupKey(req), 80));
        log.setRequestTime(LocalDateTime.now());
        log.setRequestBody(rawBody);
        return repository.saveAndFlush(log).getId();
    }

    /** Cập nhật response, tranStatus, errorCode và thời gian xử lý. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finish(Long logId, GetAccountListResponse res, String responseBody, long processTimeMs) {
        ApiRequestLog log = repository.findById(logId).orElseThrow();
        log.setResponseTime(LocalDateTime.now());
        log.setResponseBody(responseBody);
        log.setTranStatus(res.getTranStatus());
        log.setErrorCode(res.getErrorDetails() == null ? null : trunc(res.getErrorDetails().getErrorCode(), 100));
        log.setProcessTimeMs(processTimeMs);
        repository.save(log);
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String trunc(String s, int max) {
        return (s == null || s.length() <= max) ? s : s.substring(0, max);
    }
}
