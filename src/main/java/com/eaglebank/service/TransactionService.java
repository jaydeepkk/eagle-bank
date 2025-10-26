package com.eaglebank.service;

import com.eaglebank.model.Transaction;
import com.eaglebank.model.Account;
import com.eaglebank.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private AccountService accountService;

    public Transaction deposit(String accountNumber, BigDecimal amount, String userId, String reference) {
        log.info("Deposit request: accountNumber={} userId={} amount={} reference={}",
                accountNumber, userId, amount, reference);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid deposit amount={} for accountNumber={} by userId={}", amount, accountNumber, userId);
            throw new IllegalArgumentException("Amount must be positive");
        }

        Account account = accountService.findByAccountNumber(accountNumber);
        log.debug("Current balance before deposit: {} for accountNumber={}", account.getBalance(), accountNumber);

        account.setBalance(account.getBalance().add(amount));
        accountService.save(account);
        log.debug("New balance after deposit: {} for accountNumber={}", account.getBalance(), accountNumber);

        Transaction transaction = Transaction.builder()
                .id(generateTransactionId())
                .account(account)
                .amount(amount)
                .currency(account.getCurrency())
                .type("deposit")
                .reference(reference)
                .userId(userId)
                .createdTimestamp(OffsetDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Deposit transaction created successfully: transactionId={} accountNumber={} amount={}",
                saved.getId(), accountNumber, amount);

        return saved;
    }

    public Transaction withdraw(String accountNumber, BigDecimal amount, String userId, String reference) {
        log.info("Withdrawal request: accountNumber={} userId={} amount={} reference={}",
                accountNumber, userId, amount, reference);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Invalid withdrawal amount={} for accountNumber={} by userId={}", amount, accountNumber, userId);
            throw new IllegalArgumentException("Amount must be positive");
        }

        Account account = accountService.findByAccountNumber(accountNumber);
        log.debug("Current balance before withdrawal: {} for accountNumber={}", account.getBalance(), accountNumber);

        if (account.getBalance().compareTo(amount) < 0) {
            log.warn("Insufficient funds for withdrawal: accountNumber={} balance={} requestedAmount={}",
                    accountNumber, account.getBalance(), amount);
            throw new IllegalStateException("Insufficient funds");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountService.save(account);
        log.debug("New balance after withdrawal: {} for accountNumber={}", account.getBalance(), accountNumber);

        Transaction transaction = Transaction.builder()
                .id(generateTransactionId())
                .account(account)
                .amount(amount)
                .currency(account.getCurrency())
                .type("withdrawal")
                .reference(reference)
                .userId(userId)
                .createdTimestamp(OffsetDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Withdrawal transaction created successfully: transactionId={} accountNumber={} amount={}",
                saved.getId(), accountNumber, amount);

        return saved;
    }

    public List<Transaction> listForAccount(String accountNumber) {
        log.info("Listing transactions for accountNumber={}", accountNumber);
        Account account = accountService.findByAccountNumber(accountNumber);
        List<Transaction> transactions = transactionRepository.findByAccount(account);
        log.debug("Found {} transaction(s) for accountNumber={}", transactions.size(), accountNumber);
        return transactions;
    }

    public Transaction get(String txId) {
        log.info("Fetching transaction details for transactionId={}", txId);
        return transactionRepository.findById(txId)
                .orElseThrow(() -> {
                    log.error("Transaction not found for transactionId={}", txId);
                    return new RuntimeException("Transaction not found");
                });
    }

    private String generateTransactionId() {
        String txId = "tan-" + UUID.randomUUID().toString().replaceAll("-", "").substring(0, 12);
        log.trace("Generated transaction ID: {}", txId);
        return txId;
    }
}
