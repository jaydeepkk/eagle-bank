
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
class UserServiceTest {
    @InjectMocks private UserService service;

    @Mock private UserRepository userRepository;
    @Mock private AccountRepository bankAccountRepository;

    @Test @DisplayName("createUser sets id and encodes password")
    void createUser_ok() {
        User u = new User();
        u.setEmail("a@b.com");
        u.setPassword("plain");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        User saved = service.createUser(u);
        assertNotNull(saved.getId());
        assertNotEquals("plain", saved.getPassword());
    }
}
