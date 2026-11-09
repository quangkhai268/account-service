package com.example.account.server.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Object errordetails. Các trường sub* chỉ xuất hiện khi lỗi phát sinh từ ứng dụng khác (SOA...),
 * nên dùng NON_NULL để không serialize tag khi null.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {

    private String errorSource;
    private String errorCode;
    private String errorDesc;
    private String subErrorSource;
    private String subErrorCode;
    private String subErrorDesc;

    public ErrorDetails() {
    }

    public ErrorDetails(String errorSource, String errorCode, String errorDesc) {
        this.errorSource = errorSource;
        this.errorCode = errorCode;
        this.errorDesc = errorDesc;
    }

    public String getErrorSource() { return errorSource; }
    public void setErrorSource(String errorSource) { this.errorSource = errorSource; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorDesc() { return errorDesc; }
    public void setErrorDesc(String errorDesc) { this.errorDesc = errorDesc; }
    public String getSubErrorSource() { return subErrorSource; }
    public void setSubErrorSource(String subErrorSource) { this.subErrorSource = subErrorSource; }
    public String getSubErrorCode() { return subErrorCode; }
    public void setSubErrorCode(String subErrorCode) { this.subErrorCode = subErrorCode; }
    public String getSubErrorDesc() { return subErrorDesc; }
    public void setSubErrorDesc(String subErrorDesc) { this.subErrorDesc = subErrorDesc; }
}
