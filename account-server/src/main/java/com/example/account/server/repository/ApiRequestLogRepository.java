package com.example.account.server.repository;

import com.example.account.server.entity.ApiRequestLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiRequestLogRepository extends JpaRepository<ApiRequestLog, Long> {

    boolean existsByDedupKey(String dedupKey);
}
