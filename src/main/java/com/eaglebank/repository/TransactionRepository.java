package com.eaglebank.repository;

import com.eaglebank.model.Transaction;
import com.eaglebank.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {
    List<Transaction> findByAccount(Account account);
}
