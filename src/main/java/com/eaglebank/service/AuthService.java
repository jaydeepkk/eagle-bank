package com.eaglebank.service;

import com.eaglebank.security.JwtTokenProvider;
import com.eaglebank.repository.UserRepository;
import com.eaglebank.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String authenticate(String email, String password) {
        log.info("Authentication attempt for email={}", email);

        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.warn("Authentication failed — user not found for email={}", email);
                        return new RuntimeException("Invalid credentials");
                    });

            if (!encoder.matches(password, user.getPassword())) {
                log.warn("Authentication failed — invalid password for email={}", email);
                throw new RuntimeException("Invalid credentials");
            }

            String token = tokenProvider.generateToken(user.getId());
            log.info("Authentication successful for userId={} email={}", user.getId(), email);
            return token;

        } catch (RuntimeException ex) {
            log.error("Authentication error for email={}: {}", email, ex.getMessage());
            throw ex;
        } catch (Exception e) {
            log.error("Unexpected error during authentication for email={}", email, e);
            throw new RuntimeException("Authentication failed due to internal error");
        }
    }
}
