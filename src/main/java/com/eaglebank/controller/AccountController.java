package com.eaglebank.controller;

import com.eaglebank.dto.requests.CreateBankAccountRequest;
import com.eaglebank.model.Account;
import com.eaglebank.model.Transaction;
import com.eaglebank.service.AccountService;
import com.eaglebank.service.TransactionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/v1/accounts")
public class AccountController {

    private static final Logger log = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransactionService txService;

    @PostMapping
    public ResponseEntity<?> createAccount(
            @Valid @RequestBody CreateBankAccountRequest req,
            org.springframework.security.core.Authentication auth) {

        log.info("Request received to create account for userId={} with name='{}' and type='{}'",
                auth.getName(), req.getName(), req.getAccountType());

        Account a = accountService.createAccount(auth.getName(), req.getName(), req.getAccountType());

        log.info("Account created successfully with accountNumber={} for userId={}",
                a.getAccountNumber(), auth.getName());

        return ResponseEntity.status(201).body(a);
    }

    @GetMapping
    public ResponseEntity<?> listAccounts(org.springframework.security.core.Authentication auth) {
        log.info("Fetching list of accounts for userId={}", auth.getName());
        return ResponseEntity.ok(accountService.listByOwner(auth.getName()));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<?> getAccount(
            @PathVariable String accountNumber,
            org.springframework.security.core.Authentication auth) {

        log.info("Fetching account details for accountNumber={} by userId={}", accountNumber, auth.getName());
        try {
            Account a = accountService.findByAccountNumber(accountNumber);
            if (!a.getOwner().getId().equals(auth.getName())) {
                log.warn("Unauthorized access attempt by userId={} for accountNumber={}", auth.getName(), accountNumber);
                return ResponseEntity.status(403).body(java.util.Map.of("message", "forbidden"));
            }
            log.debug("Account retrieved successfully: accountNumber={}", accountNumber);
            return ResponseEntity.ok(a);
        } catch (RuntimeException ex) {
            log.error("Account not found for accountNumber={} requested by userId={}", accountNumber, auth.getName(), ex);
            return ResponseEntity.status(404).body(java.util.Map.of("message", "not found"));
        }
    }

    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<?> deleteAccount(
            @PathVariable String accountNumber,
            org.springframework.security.core.Authentication auth) {

        log.info("Delete request received for accountNumber={} by userId={}", accountNumber, auth.getName());
        try {
            Account account = accountService.findByAccountNumber(accountNumber);
            if (!account.getOwner().getId().equals(auth.getName())) {
                log.warn("UserId={} attempted to delete unauthorized accountNumber={}", auth.getName(), accountNumber);
                return ResponseEntity.status(403).body(java.util.Map.of("message", "forbidden"));
            }
            List<Transaction> transactions = txService.listForAccount(accountNumber);
            // 🧩 Check if account has transactions
            if (transactions != null && !transactions.isEmpty()) {
                log.warn("Cannot delete accountNumber={} — existing transactions found (count={})",
                        accountNumber, transactions.size());
                return ResponseEntity.status(409)
                        .body(Map.of("message", "Cannot delete account with existing transactions"));
            }
            accountService.accountRepository.delete(account);
            log.info("Account deleted successfully: accountNumber={} by userId={}", accountNumber, auth.getName());
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            log.error("Account deletion failed: accountNumber={} by userId={}", accountNumber, auth.getName(), ex);
            return ResponseEntity.status(404).body(java.util.Map.of("message", "not found"));
        }
    }
}
