package com.example.account.server.service;

import com.example.account.server.dto.GetAccountListRequest;
import com.example.account.server.dto.GetAccountListResponse;
import com.example.account.server.exception.BusinessException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * Điều phối: parse body -> ghi log -> chặn trùng requestId -> xử lý -> cập nhật log.
 * Class này KHÔNG @Transactional để từng bước log commit độc lập.
 */
@Service
public class AccountRequestProcessor {

    /** requestId (theo từng appCode) đã được gửi trước đó. Mã bổ sung, đặc tả chưa quy định. */
    public static final String DUP001 = "DUP001";
    public static final String SYS999 = "SYS999";

    private static final Logger log = LoggerFactory.getLogger(AccountRequestProcessor.class);

    private final AccountService accountService;
    private final ApiRequestLogService logService;
    private final ObjectMapper objectMapper;

    public AccountRequestProcessor(AccountService accountService,
                                   ApiRequestLogService logService,
                                   ObjectMapper objectMapper) {
        this.accountService = accountService;
        this.logService = logService;
        this.objectMapper = objectMapper;
    }

    public GetAccountListResponse process(String rawBody) {
        long start = System.nanoTime();

        // 1. Parse (lỗi parse không làm mất log: req = null, vẫn ghi rawBody)
        GetAccountListRequest req = null;
        boolean parseError = false;
        try {
            req = objectMapper.readValue(rawBody, GetAccountListRequest.class);
        } catch (Exception e) {
            parseError = true;
        }

        // 2. Ghi log + giữ chỗ requestId (nếu trùng thì ghi log với dedupKey = null)
        boolean duplicate = logService.isDuplicate(req);
        Long logId;
        try {
            logId = logService.register(req, rawBody, duplicate);
        } catch (DataIntegrityViolationException e) {
            // hai request trùng đến cùng lúc, unique index chặn request đến sau
            duplicate = true;
            logId = logService.register(req, rawBody, true);
        }

        // 3. Xử lý
        GetAccountListResponse res;
        String requestId = req == null ? null : req.getRequestId();
        if (parseError || req == null) {
            res = ResponseFactory.fail(null, RequestValidator.VAL002, "Request body không hợp lệ");
        } else if (duplicate) {
            res = ResponseFactory.fail(requestId, DUP001, "requestId đã được gửi trước đó");
        } else {
            try {
                res = accountService.getAccountList(req);
            } catch (BusinessException e) {
                res = ResponseFactory.fail(requestId, e.getErrorCode(), e.getMessage());
            } catch (Exception e) {
                log.error("Lỗi xử lý requestId={}", requestId, e);
                res = ResponseFactory.fail(requestId, SYS999, "Lỗi hệ thống");
            }
        }

        // 4. Cập nhật log (lỗi ghi log không được làm hỏng response trả về)
        try {
            long ms = (System.nanoTime() - start) / 1_000_000;
            logService.finish(logId, res, objectMapper.writeValueAsString(res), ms);
        } catch (Exception e) {
            log.error("Không cập nhật được log id={}", logId, e);
        }
        return res;
    }
}
