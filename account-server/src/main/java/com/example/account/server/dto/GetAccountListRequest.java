package com.example.account.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Input của API getAccountList. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class GetAccountListRequest {

    /** Mã phiên request (Y, 50). */
    private String requestId;
    /** Thời gian nhận request, yyyyMMddHHmmss (Y, 30). */
    private String processTime;
    /** Mã app_id cấp cho client (Y, 20). */
    private String appCode;
    /** Mã CIF (N, 20). Bắt buộc có cifNo hoặc idNo. */
    private String cifNo;
    /** Số ID (N, 40). Bắt buộc có cifNo hoặc idNo. */
    private String idNo;

    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getProcessTime() { return processTime; }
    public void setProcessTime(String processTime) { this.processTime = processTime; }
    public String getAppCode() { return appCode; }
    public void setAppCode(String appCode) { this.appCode = appCode; }
    public String getCifNo() { return cifNo; }
    public void setCifNo(String cifNo) { this.cifNo = cifNo; }
    public String getIdNo() { return idNo; }
    public void setIdNo(String idNo) { this.idNo = idNo; }
}
