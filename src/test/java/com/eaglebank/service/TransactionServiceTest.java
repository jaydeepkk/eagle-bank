
package com.eaglebank.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import com.eaglebank.model.*;
import com.eaglebank.repository.*;
import java.math.BigDecimal;
import java.util.Optional;
@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @InjectMocks private TransactionService service;

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountService accountService;

    @Test @DisplayName("deposit increases balance")
    void deposit_ok() {
        Account a = new Account(); a.setAccountNumber("01000001"); a.setCurrency("GBP"); a.setBalance(new BigDecimal("0.00"));
        when(accountService.findByAccountNumber(eq("01000001"))).thenReturn(a);
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));
        Transaction t = service.deposit("01000001", new BigDecimal("5.00"), "usr-1", "ref");
        assertEquals(new BigDecimal("5.00"), a.getBalance());
        assertEquals("deposit", t.getType());
    }
}
