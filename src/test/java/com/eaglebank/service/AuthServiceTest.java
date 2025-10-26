
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
class AuthServiceTest {
    @InjectMocks private AuthService service;

    @Mock private UserRepository userRepository;
    @Mock private com.eaglebank.security.JwtTokenProvider tokenProvider;

    @Test @DisplayName("authenticate invalid creds throws")
    void auth_invalid() {
        when(userRepository.findByEmail(eq("e@x.com"))).thenReturn(java.util.Optional.empty());
        assertThrows(RuntimeException.class, () -> service.authenticate("e@x.com", "pwd"));
    }
}
