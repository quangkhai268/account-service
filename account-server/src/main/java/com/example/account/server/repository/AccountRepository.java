package com.example.account.server.repository;

import com.example.account.server.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String> {

    List<Account> findByCifNoOrderByAcctNo(String cifNo);

    List<Account> findByIdNoOrderByAcctNo(String idNo);

    List<Account> findByCifNoAndIdNoOrderByAcctNo(String cifNo, String idNo);
}
