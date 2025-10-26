
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
class AccountServiceTest {
    @InjectMocks private AccountService service;

    @Mock private AccountRepository accountRepository;
    @Mock private UserRepository userRepository;

    @Test @DisplayName("createAccount saves with generated number")
    void create_ok() {
        User owner = new User(); owner.setId("usr-abc123");
        when(userRepository.findById(eq("usr-abc123"))).thenReturn(Optional.of(owner));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        Account acc = service.createAccount("usr-abc123", "My", "personal");
        assertNotNull(acc.getAccountNumber());
    }
}
