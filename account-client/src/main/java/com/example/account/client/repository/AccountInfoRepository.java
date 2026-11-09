package com.example.account.client.repository;

import com.example.account.client.entity.AccountInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountInfoRepository extends JpaRepository<AccountInfo, Long> {
    List<AccountInfo> findByRequestId(String requestId);
}
