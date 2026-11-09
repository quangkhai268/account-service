package com.example.account.server.service;

import com.example.account.server.dto.AccountData;
import com.example.account.server.dto.GetAccountListRequest;
import com.example.account.server.dto.GetAccountListResponse;
import com.example.account.server.entity.Account;
import com.example.account.server.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final RequestValidator validator;

    public AccountService(AccountRepository accountRepository, RequestValidator validator) {
        this.accountRepository = accountRepository;
        this.validator = validator;
    }

    @Transactional(readOnly = true)
    public GetAccountListResponse getAccountList(GetAccountListRequest req) {
        validator.validate(req);

        String cifNo = trimToNull(req.getCifNo());
        String idNo = trimToNull(req.getIdNo());

        List<Account> accounts;
        if (cifNo != null && idNo != null) {
            accounts = accountRepository.findByCifNoAndIdNoOrderByAcctNo(cifNo, idNo);
        } else if (cifNo != null) {
            accounts = accountRepository.findByCifNoOrderByAcctNo(cifNo);
        } else {
            accounts = accountRepository.findByIdNoOrderByAcctNo(idNo);
        }

        GetAccountListResponse res = newResponse(req.getRequestId());
        res.setTranStatus(GetAccountListResponse.STATUS_SUCCESS);
        res.setTranDesc("Success");
        res.setData(accounts.stream().map(this::toData).toList());
        return res;
    }

    public static GetAccountListResponse newResponse(String requestId) {
        GetAccountListResponse res = new GetAccountListResponse();
        res.setRequestId(requestId);
        res.setProcessTime(LocalDateTime.now().format(RequestValidator.TIME_FORMAT));
        return res;
    }

    private AccountData toData(Account a) {
        AccountData d = new AccountData();
        d.setAcctNo(a.getAcctNo());
        d.setAcctType(a.getAcctType());
        d.setAcctTitle(a.getAcctTitle());
        d.setCurrCode(a.getCurrCode());
        d.setBranchCode(a.getBranchCode());
        d.setLedgerBalance(a.getLedgerBalance() == null ? null : a.getLedgerBalance().toPlainString());
        d.setMinBalance(a.getMinBalance() == null ? null : a.getMinBalance().toPlainString());
        d.setAccStatus(a.getAccStatus());
        return d;
    }

    private String trimToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
