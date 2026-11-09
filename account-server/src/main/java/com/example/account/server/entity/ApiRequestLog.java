package com.example.account.server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Log mỗi request nhận được. */
@Entity
@Table(name = "API_REQUEST_LOG")
public class ApiRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    /** Có thể null nếu request thiếu requestId hoặc body không parse được. */
    @Column(name = "REQUEST_ID", length = 50)
    private String requestId;

    @Column(name = "APP_CODE", length = 20)
    private String appCode;

    /**
     * Khóa chống trùng = appCode|requestId, UNIQUE. Chỉ request đầu tiên (được chấp nhận) mới có giá trị;
     * các request bị từ chối vì trùng để null (PostgreSQL cho phép nhiều NULL trong unique constraint)
     * nên vẫn ghi log được.
     */
    @Column(name = "DEDUP_KEY", length = 80, unique = true)
    private String dedupKey;

    @Column(name = "REQUEST_TIME", nullable = false)
    private LocalDateTime requestTime;

    @Column(name = "REQUEST_BODY", columnDefinition = "text")
    private String requestBody;

    @Column(name = "RESPONSE_TIME")
    private LocalDateTime responseTime;

    @Column(name = "RESPONSE_BODY", columnDefinition = "text")
    private String responseBody;

    @Column(name = "TRAN_STATUS", length = 1)
    private String tranStatus;

    @Column(name = "ERROR_CODE", length = 100)
    private String errorCode;

    /** Thời gian xử lý (ms). */
    @Column(name = "PROCESS_TIME_MS")
    private Long processTimeMs;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getAppCode() { return appCode; }
    public void setAppCode(String appCode) { this.appCode = appCode; }
    public String getDedupKey() { return dedupKey; }
    public void setDedupKey(String dedupKey) { this.dedupKey = dedupKey; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }
    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
    public LocalDateTime getResponseTime() { return responseTime; }
    public void setResponseTime(LocalDateTime responseTime) { this.responseTime = responseTime; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public String getTranStatus() { return tranStatus; }
    public void setTranStatus(String tranStatus) { this.tranStatus = tranStatus; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public Long getProcessTimeMs() { return processTimeMs; }
    public void setProcessTimeMs(Long processTimeMs) { this.processTimeMs = processTimeMs; }
}
