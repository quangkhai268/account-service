package com.example.account.server.service;

import com.example.account.server.dto.ErrorDetails;
import com.example.account.server.dto.GetAccountListResponse;

import java.time.LocalDateTime;

/** Tạo response lỗi theo đặc tả (tranStatus=1 + errordetails). */
public final class ResponseFactory {

    public static final String ERROR_SOURCE = "PMH";

    private ResponseFactory() {
    }

    public static GetAccountListResponse fail(String requestId, String code, String desc) {
        GetAccountListResponse res = new GetAccountListResponse();
        res.setRequestId(requestId);
        res.setProcessTime(LocalDateTime.now().format(RequestValidator.TIME_FORMAT));
        res.setTranStatus(GetAccountListResponse.STATUS_FAIL);
        res.setTranDesc(desc);
        res.setErrorDetails(new ErrorDetails(ERROR_SOURCE, code, desc));
        return res;
    }
}
