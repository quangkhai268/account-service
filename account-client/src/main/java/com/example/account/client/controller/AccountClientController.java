package com.example.account.client.controller;

import com.example.account.client.dto.GetAccountListResponse;
import com.example.account.client.service.AccountClientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Endpoint để kích hoạt gọi API (ví dụ: GET /client/accounts?cifNo=CIF001). */
@RestController
@RequestMapping("/client/accounts")
public class AccountClientController {

    private final AccountClientService service;

    public AccountClientController(AccountClientService service) {
        this.service = service;
    }

    @GetMapping
    public GetAccountListResponse query(@RequestParam(required = false) String cifNo,
                                        @RequestParam(required = false) String idNo) {
        return service.getAccountList(cifNo, idNo);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> badRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> internal(RuntimeException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Đã gọi API nhưng không lưu được dữ liệu vào DB: " + e.getMessage()));
    }
}
