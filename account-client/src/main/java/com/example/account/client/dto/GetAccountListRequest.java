package com.example.account.client.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/** Request gửi sang server. cifNo / idNo null sẽ không được serialize. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetAccountListRequest {

    private String requestId;
    private String processTime;
    private String appCode;
    private String cifNo;
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
