package com.example.account.server.controller;

import com.example.account.server.dto.GetAccountListResponse;
import com.example.account.server.service.AccountRequestProcessor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountRequestProcessor processor;

    public AccountController(AccountRequestProcessor processor) {
        this.processor = processor;
    }

    /**
     * POST /api/v1/accounts/getAccountList (URI trong đặc tả để trống nên tự đặt).
     * Nhận body dạng String để log được nguyên văn, kể cả khi JSON sai.
     */
    @PostMapping(value = "/getAccountList", produces = "application/json")
    public GetAccountListResponse getAccountList(@RequestBody(required = false) String body) {
        return processor.process(body == null ? "" : body);
    }
}
