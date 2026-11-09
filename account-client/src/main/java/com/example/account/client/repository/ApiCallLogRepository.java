package com.example.account.client.repository;

import com.example.account.client.entity.ApiCallLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApiCallLogRepository extends JpaRepository<ApiCallLog, Long> {
    Optional<ApiCallLog> findByRequestId(String requestId);
}
