package com.example.account.client.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** Log mỗi lần gọi API: ghi PENDING trước khi gọi, cập nhật kết quả sau khi gọi. */
@Entity
@Table(name = "API_CALL_LOG")
public class ApiCallLog {

    public static final String PENDING = "PENDING";
    public static final String SUCCESS = "SUCCESS";
    public static final String FAILED = "FAILED";
    public static final String TIMEOUT = "TIMEOUT";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "REQUEST_ID", length = 50, nullable = false)
    private String requestId;

    @Column(name = "APP_CODE", length = 20)
    private String appCode;

    @Column(name = "ENDPOINT", length = 500)
    private String endpoint;

    @Column(name = "REQUEST_TIME", nullable = false)
    private LocalDateTime requestTime;

    @Column(name = "REQUEST_BODY", columnDefinition = "text")
    private String requestBody;

    @Column(name = "RESPONSE_TIME")
    private LocalDateTime responseTime;

    @Column(name = "RESPONSE_BODY", columnDefinition = "text")
    private String responseBody;

    @Column(name = "HTTP_STATUS")
    private Integer httpStatus;

    /** Giá trị tranStatus của response (0/1/2). */
    @Column(name = "TRAN_STATUS", length = 1)
    private String tranStatus;

    @Column(name = "TRAN_DESC", length = 200)
    private String tranDesc;

    @Column(name = "ERROR_CODE", length = 100)
    private String errorCode;

    /** PENDING / SUCCESS / FAILED / TIMEOUT. */
    @Column(name = "STATUS", length = 10, nullable = false)
    private String status;

    @Column(name = "DURATION_MS")
    private Long durationMs;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getAppCode() { return appCode; }
    public void setAppCode(String appCode) { this.appCode = appCode; }
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    public LocalDateTime getRequestTime() { return requestTime; }
    public void setRequestTime(LocalDateTime requestTime) { this.requestTime = requestTime; }
    public String getRequestBody() { return requestBody; }
    public void setRequestBody(String requestBody) { this.requestBody = requestBody; }
    public LocalDateTime getResponseTime() { return responseTime; }
    public void setResponseTime(LocalDateTime responseTime) { this.responseTime = responseTime; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
    public Integer getHttpStatus() { return httpStatus; }
    public void setHttpStatus(Integer httpStatus) { this.httpStatus = httpStatus; }
    public String getTranStatus() { return tranStatus; }
    public void setTranStatus(String tranStatus) { this.tranStatus = tranStatus; }
    public String getTranDesc() { return tranDesc; }
    public void setTranDesc(String tranDesc) { this.tranDesc = tranDesc; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
}
