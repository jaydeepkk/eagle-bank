package com.eaglebank.controller;

import com.eaglebank.dto.requests.CreateTransactionRequest;
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

@RestController
@RequestMapping("/v1/accounts/{accountNumber}/transactions")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService txService;

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<?> createTransaction(
            @PathVariable String accountNumber,
            @Valid @RequestBody CreateTransactionRequest req,
            org.springframework.security.core.Authentication auth) {

        log.info("Create transaction request received: accountNumber={}, type={}, amount={}, userId={}",
                accountNumber, req.getType(), req.getAmount(), auth.getName());

        Account account = accountService.findByAccountNumber(accountNumber);
        if (!account.getOwner().getId().equals(auth.getName())) {
            log.warn("Unauthorized transaction attempt by userId={} on accountNumber={}", auth.getName(), accountNumber);
            return ResponseEntity.status(403).body(java.util.Map.of("message", "forbidden"));
        }

        try {
            Transaction transaction;
            if ("deposit".equalsIgnoreCase(req.getType())) {
                log.debug("Processing deposit for accountNumber={} amount={}", accountNumber, req.getAmount());
                transaction = txService.deposit(accountNumber, req.getAmount(), auth.getName(), req.getReference());
            } else if ("withdrawal".equalsIgnoreCase(req.getType())) {
                log.debug("Processing withdrawal for accountNumber={} amount={}", accountNumber, req.getAmount());
                transaction = txService.withdraw(accountNumber, req.getAmount(), auth.getName(), req.getReference());
            } else {
                log.error("Invalid transaction type '{}' provided for accountNumber={}", req.getType(), accountNumber);
                return ResponseEntity.badRequest().body(java.util.Map.of("message", "invalid type"));
            }

            log.info("Transaction created successfully: transactionId={} accountNumber={} type={}",
                    transaction.getId(), accountNumber, transaction.getType());
            return ResponseEntity.status(201).body(transaction);

        } catch (IllegalStateException ise) {
            log.warn("Transaction failed due to insufficient funds: accountNumber={} message={}",
                    accountNumber, ise.getMessage());
            return ResponseEntity.unprocessableEntity().body(java.util.Map.of("message", ise.getMessage()));
        } catch (IllegalArgumentException iae) {
            log.error("Transaction request invalid: accountNumber={} message={}", accountNumber, iae.getMessage());
            return ResponseEntity.badRequest().body(java.util.Map.of("message", iae.getMessage()));
        } catch (Exception ex) {
            log.error("Unexpected error while creating transaction for accountNumber={} userId={}",
                    accountNumber, auth.getName(), ex);
            return ResponseEntity.internalServerError().body(java.util.Map.of("message", "unexpected error"));
        }
    }

    @GetMapping
    public ResponseEntity<?> listTransactions(
            @PathVariable String accountNumber,
            org.springframework.security.core.Authentication auth) {

        log.info("Fetching transactions for accountNumber={} userId={}", accountNumber, auth.getName());

        Account account = accountService.findByAccountNumber(accountNumber);
        if (!account.getOwner().getId().equals(auth.getName())) {
            log.warn("Unauthorized attempt to list transactions for accountNumber={} by userId={}", accountNumber, auth.getName());
            return ResponseEntity.status(403).body(java.util.Map.of("message", "forbidden"));
        }

        List<Transaction> transactions = txService.listForAccount(accountNumber);
        log.debug("Retrieved {} transactions for accountNumber={}", transactions.size(), accountNumber);

        return ResponseEntity.ok(java.util.Map.of("transactions", transactions));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<?> getTransaction(
            @PathVariable String accountNumber,
            @PathVariable String transactionId,
            org.springframework.security.core.Authentication auth) {

        log.info("Fetching transactionId={} for accountNumber={} by userId={}", transactionId, accountNumber, auth.getName());

        Account account = accountService.findByAccountNumber(accountNumber);
        if (!account.getOwner().getId().equals(auth.getName())) {
            log.warn("Unauthorized attempt to access transactionId={} for accountNumber={} by userId={}",
                    transactionId, accountNumber, auth.getName());
            return ResponseEntity.status(403).body(java.util.Map.of("message", "forbidden"));
        }

        Transaction transaction = txService.get(transactionId);
        if (!transaction.getAccount().getAccountNumber().equals(accountNumber)) {
            log.error("TransactionId={} does not belong to accountNumber={}", transactionId, accountNumber);
            return ResponseEntity.status(404).body(java.util.Map.of("message", "not found"));
        }

        log.debug("Transaction fetched successfully: transactionId={} accountNumber={}", transactionId, accountNumber);
        return ResponseEntity.ok(transaction);
    }
}
