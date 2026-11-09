package com.example.account.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetAccountListResponse {

    public static final String STATUS_SUCCESS = "0";
    public static final String STATUS_FAIL = "1";
    public static final String STATUS_TIMEOUT = "2";

    private String processTime;
    private String requestId;
    /** 0: success; 1: fail; 2: TimeOut. */
    private String tranStatus;
    private String tranDesc;
    @JsonProperty("errordetails")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private ErrorDetails errorDetails;
    private List<AccountData> data = new ArrayList<>();

    public String getProcessTime() { return processTime; }
    public void setProcessTime(String processTime) { this.processTime = processTime; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getTranStatus() { return tranStatus; }
    public void setTranStatus(String tranStatus) { this.tranStatus = tranStatus; }
    public String getTranDesc() { return tranDesc; }
    public void setTranDesc(String tranDesc) { this.tranDesc = tranDesc; }
    public ErrorDetails getErrorDetails() { return errorDetails; }
    public void setErrorDetails(ErrorDetails errorDetails) { this.errorDetails = errorDetails; }
    public List<AccountData> getData() { return data; }
    public void setData(List<AccountData> data) { this.data = data == null ? new ArrayList<>() : data; }
}
