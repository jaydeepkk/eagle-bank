package com.eaglebank.service;

import com.eaglebank.model.Account;
import com.eaglebank.model.User;
import com.eaglebank.repository.AccountRepository;
import com.eaglebank.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Random;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    public AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    public Account createAccount(String ownerId, String name, String accountType) {
        log.info("Creating account for ownerId={} with name='{}' and type='{}'", ownerId, name, accountType);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    log.error("Account creation failed — user not found for ownerId={}", ownerId);
                    return new RuntimeException("User not found");
                });

        String accountNumber = generateAccountNumber();
        log.debug("Generated new accountNumber={} for ownerId={}", accountNumber, ownerId);

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .sortCode("10-10-10")
                .name(name)
                .accountType(accountType == null ? "personal" : accountType)
                .owner(owner)
                .currency("GBP")
                .balance(BigDecimal.ZERO)
                .createdTimestamp(OffsetDateTime.now())
                .updatedTimestamp(OffsetDateTime.now())
                .build();

        Account saved = accountRepository.save(account);
        log.info("Account successfully created: accountNumber={} for ownerId={}", saved.getAccountNumber(), ownerId);
        return saved;
    }

    public List<Account> listByOwner(String ownerId) {
        log.info("Listing accounts for ownerId={}", ownerId);

        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> {
                    log.error("Cannot list accounts — user not found for ownerId={}", ownerId);
                    return new RuntimeException("User not found");
                });

        List<Account> accounts = accountRepository.findByOwner(owner);
        log.debug("Found {} account(s) for ownerId={}", accounts.size(), ownerId);

        return accounts;
    }

    public Account findByAccountNumber(String accountNumber) {
        log.info("Fetching account details for accountNumber={}", accountNumber);

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("Account not found for accountNumber={}", accountNumber);
                    return new RuntimeException("Account not found");
                });
    }

    public Account save(Account account) {
        log.info("Saving account updates for accountNumber={}", account.getAccountNumber());
        account.setUpdatedTimestamp(OffsetDateTime.now());
        Account updated = accountRepository.save(account);
        log.debug("Account updated successfully: accountNumber={} at {}", updated.getAccountNumber(), updated.getUpdatedTimestamp());
        return updated;
    }

    private String generateAccountNumber() {
        Random random = new Random();
        int sixDigits = random.nextInt(1_000_000);
        String accountNumber = String.format("01%06d", sixDigits);
        log.trace("Generated account number {}", accountNumber);
        return accountNumber;
    }
}
