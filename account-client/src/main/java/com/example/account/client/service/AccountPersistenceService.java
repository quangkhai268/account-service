package com.example.account.client.service;

import com.example.account.client.dto.AccountData;
import com.example.account.client.dto.GetAccountListRequest;
import com.example.account.client.dto.GetAccountListResponse;
import com.example.account.client.entity.AccountInfo;
import com.example.account.client.entity.ApiCallLog;
import com.example.account.client.repository.AccountInfoRepository;
import com.example.account.client.repository.ApiCallLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lưu kết quả response: cập nhật API_CALL_LOG và insert ACCOUNT_INFO trong cùng một
 * transaction, nên hoặc cả hai thành công, hoặc cả hai rollback.
 */
@Service
public class AccountPersistenceService {

    private static final Logger log = LoggerFactory.getLogger(AccountPersistenceService.class);

    private final ApiCallLogRepository logRepository;
    private final AccountInfoRepository accountRepository;
    private final ApiLogService apiLogService;

    public AccountPersistenceService(ApiCallLogRepository logRepository,
                                     AccountInfoRepository accountRepository,
                                     ApiLogService apiLogService) {
        this.logRepository = logRepository;
        this.accountRepository = accountRepository;
        this.apiLogService = apiLogService;
    }

    @Transactional
    public int saveSuccess(Long logId, GetAccountListRequest req, GetAccountListResponse res,
                           int httpStatus, String responseBody, long durationMs) {
        ApiCallLog callLog = logRepository.findById(logId).orElseThrow();

        LocalDateTime now = LocalDateTime.now();
        List<AccountInfo> rows = new ArrayList<>();
        for (AccountData d : res.getData()) {
            AccountInfo a = new AccountInfo();
            a.setRequestId(req.getRequestId());
            a.setCifNo(req.getCifNo());
            a.setIdNo(req.getIdNo());
            a.setAcctNo(d.getAcctNo());
            a.setAcctType(d.getAcctType());
            a.setAcctTitle(d.getAcctTitle());
            a.setCurrCode(d.getCurrCode());
            a.setBranchCode(d.getBranchCode());
            a.setLedgerBalance(toDecimal(d.getLedgerBalance(), "ledgerBalance", d.getAcctNo()));
            a.setMinBalance(toDecimal(d.getMinBalance(), "minBalance", d.getAcctNo()));
            a.setAccStatus(d.getAccStatus());
            a.setCreatedAt(now);
            rows.add(a);
        }
        accountRepository.saveAll(rows);

        apiLogService.apply(callLog, ApiCallLog.SUCCESS, httpStatus, responseBody,
                res.getTranStatus(), res.getTranDesc(), null, durationMs);
        logRepository.save(callLog);
        return rows.size();
    }

    private BigDecimal toDecimal(String value, String field, String acctNo) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            log.warn("Giá trị {} không hợp lệ ('{}') cho tài khoản {}, lưu null", field, value, acctNo);
            return null;
        }
    }
}
