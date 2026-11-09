package com.example.account.client.service;

import com.example.account.client.config.AccountApiProperties;
import com.example.account.client.dto.ErrorDetails;
import com.example.account.client.dto.GetAccountListRequest;
import com.example.account.client.dto.GetAccountListResponse;
import com.example.account.client.entity.ApiCallLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Luồng: (1) tạo request -> (2) ghi log DB (PENDING, commit ngay) -> (3) gọi API
 * -> (4) cập nhật log + insert dữ liệu response vào DB.
 * Class này cố ý KHÔNG đánh dấu @Transactional để các bước DB (ở bean khác) tự commit độc lập.
 */
@Service
public class AccountClientService {

    private static final Logger log = LoggerFactory.getLogger(AccountClientService.class);
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final String SOURCE_CLIENT = "CLIENT";

    private final RestTemplate restTemplate;
    private final AccountApiProperties props;
    private final ApiLogService apiLogService;
    private final AccountPersistenceService persistenceService;
    private final ObjectMapper objectMapper;

    public AccountClientService(RestTemplate accountRestTemplate,
                                AccountApiProperties props,
                                ApiLogService apiLogService,
                                AccountPersistenceService persistenceService,
                                ObjectMapper objectMapper) {
        this.restTemplate = accountRestTemplate;
        this.props = props;
        this.apiLogService = apiLogService;
        this.persistenceService = persistenceService;
        this.objectMapper = objectMapper;
    }

    public GetAccountListResponse getAccountList(String cifNo, String idNo) {
        if (isBlank(cifNo) && isBlank(idNo)) {
            throw new IllegalArgumentException("Phải truyền cifNo hoặc idNo");
        }

        // 1. Build request
        GetAccountListRequest req = new GetAccountListRequest();
        req.setRequestId(UUID.randomUUID().toString().replace("-", ""));
        req.setProcessTime(LocalDateTime.now().format(TIME_FORMAT));
        req.setAppCode(props.getAppCode());
        req.setCifNo(isBlank(cifNo) ? null : cifNo.trim());
        req.setIdNo(isBlank(idNo) ? null : idNo.trim());

        String requestJson = toJson(req);

        // 2. Ghi log TRƯỚC khi gọi API (commit độc lập)
        Long logId = apiLogService.start(req, props.getUrl(), requestJson);

        // 3. Gọi API
        long start = System.nanoTime();
        String rawBody = null;
        Integer httpStatus = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
            ResponseEntity<String> entity = restTemplate.postForEntity(
                    props.getUrl(), new HttpEntity<>(requestJson, headers), String.class);
            httpStatus = entity.getStatusCode().value();
            rawBody = entity.getBody();
        } catch (HttpStatusCodeException e) {
            // 4xx / 5xx
            return failLocal(logId, ApiCallLog.FAILED, e.getStatusCode().value(), e.getResponseBodyAsString(),
                    GetAccountListResponse.STATUS_FAIL, "CLI_HTTP_" + e.getStatusCode().value(),
                    "Server trả về HTTP " + e.getStatusCode().value(), req, start);
        } catch (ResourceAccessException e) {
            if (isTimeout(e)) {
                return failLocal(logId, ApiCallLog.TIMEOUT, null, null,
                        GetAccountListResponse.STATUS_TIMEOUT, "CLI_TIMEOUT",
                        "Timeout khi gọi API", req, start);
            }
            return failLocal(logId, ApiCallLog.FAILED, null, null,
                    GetAccountListResponse.STATUS_FAIL, "CLI_CONNECT_ERROR",
                    "Không kết nối được tới server: " + e.getMessage(), req, start);
        } catch (RestClientException e) {
            return failLocal(logId, ApiCallLog.FAILED, null, null,
                    GetAccountListResponse.STATUS_FAIL, "CLI_ERROR",
                    "Lỗi khi gọi API: " + e.getMessage(), req, start);
        }

        long durationMs = elapsedMs(start);

        // 4. Parse response
        GetAccountListResponse res;
        try {
            res = objectMapper.readValue(rawBody, GetAccountListResponse.class);
        } catch (Exception e) {
            return failLocal(logId, ApiCallLog.FAILED, httpStatus, rawBody,
                    GetAccountListResponse.STATUS_FAIL, "CLI_PARSE_ERROR",
                    "Response không đúng định dạng", req, start);
        }

        if (res.getRequestId() != null && !res.getRequestId().equals(req.getRequestId())) {
            log.warn("requestId response ({}) khác request ({})", res.getRequestId(), req.getRequestId());
        }

        // 5. Lưu kết quả vào DB
        if (GetAccountListResponse.STATUS_SUCCESS.equals(res.getTranStatus())) {
            try {
                int n = persistenceService.saveSuccess(logId, req, res, httpStatus, rawBody, durationMs);
                log.info("requestId={} lưu {} tài khoản", req.getRequestId(), n);
            } catch (RuntimeException e) {
                log.error("Lỗi insert dữ liệu requestId={}", req.getRequestId(), e);
                apiLogService.fail(logId, ApiCallLog.FAILED, httpStatus, rawBody,
                        res.getTranStatus(), "Lỗi lưu DB: " + e.getMessage(), "CLI_DB_ERROR", durationMs);
                throw e;
            }
        } else {
            String status = GetAccountListResponse.STATUS_TIMEOUT.equals(res.getTranStatus())
                    ? ApiCallLog.TIMEOUT : ApiCallLog.FAILED;
            String errorCode = res.getErrorDetails() == null ? null : res.getErrorDetails().getErrorCode();
            apiLogService.fail(logId, status, httpStatus, rawBody,
                    res.getTranStatus(), res.getTranDesc(), errorCode, durationMs);
        }
        return res;
    }

    private GetAccountListResponse failLocal(Long logId, String status, Integer httpStatus, String body,
                                             String tranStatus, String code, String desc,
                                             GetAccountListRequest req, long startNano) {
        long durationMs = elapsedMs(startNano);
        log.warn("requestId={} lỗi {}: {}", req.getRequestId(), code, desc);
        apiLogService.fail(logId, status, httpStatus, body, tranStatus, desc, code, durationMs);

        GetAccountListResponse res = new GetAccountListResponse();
        res.setRequestId(req.getRequestId());
        res.setProcessTime(LocalDateTime.now().format(TIME_FORMAT));
        res.setTranStatus(tranStatus);
        res.setTranDesc(desc);
        res.setErrorDetails(new ErrorDetails(SOURCE_CLIENT, code, desc));
        return res;
    }

    private boolean isTimeout(Throwable e) {
        for (Throwable t = e; t != null; t = t.getCause()) {
            if (t instanceof SocketTimeoutException) {
                return true;
            }
        }
        return false;
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Không serialize được request", e);
        }
    }

    private long elapsedMs(long startNano) {
        return (System.nanoTime() - startNano) / 1_000_000;
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
