package com.eaglebank.service;

import com.eaglebank.exception.ResourceNotFoundException;
import com.eaglebank.exception.UnauthorizedException;
import com.eaglebank.model.User;
import com.eaglebank.repository.AccountRepository;
import com.eaglebank.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository bankAccountRepository;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public User createUser(User user) {
        log.info("Creating new user with email={}", user.getEmail());

        String userId = "usr-" + randomAlphaNum(8);
        user.setId(userId);
        user.setPassword(encoder.encode(user.getPassword()));
        user.setCreatedTimestamp(OffsetDateTime.now());
        user.setUpdatedTimestamp(OffsetDateTime.now());

        User saved = userRepository.save(user);
        log.info("User created successfully: userId={} email={}", saved.getId(), saved.getEmail());

        return saved;
    }

    public Optional<User> findById(String id) {
        log.debug("Fetching user by id={}", id);
        Optional<User> user = userRepository.findById(id);
        if (user.isEmpty()) {
            log.warn("User not found for id={}", id);
        } else {
            log.debug("User found: id={} email={}", id, user.get().getEmail());
        }
        return user;
    }

    public Optional<User> findByEmail(String email) {
        log.debug("Fetching user by email={}", email);
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            log.warn("User not found for email={}", email);
        } else {
            log.debug("User found: id={} email={}", user.get().getId(), email);
        }
        return user;
    }

    private String randomAlphaNum(int n) {
        String val = UUID.randomUUID().toString().replaceAll("-", "").substring(0, n);
        log.trace("Generated random alphanumeric value: {}", val);
        return val;
    }

    @Transactional
    public void deleteUser(String userId, String authenticatedUserId) {
        log.info("Delete request for userId={} by authenticatedUserId={}", userId, authenticatedUserId);

        // 1️⃣ Check user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User deletion failed — user not found: userId={}", userId);
                    return new ResourceNotFoundException("User not found: " + userId);
                });

        // 2️⃣ Verify identity
        if (!user.getId().equals(authenticatedUserId)) {
            log.warn("Unauthorized delete attempt: userId={} by caller={}", userId, authenticatedUserId);
            throw new UnauthorizedException("You cannot delete another user's profile");
        }

        // 3️⃣ Check linked accounts
        if (!bankAccountRepository.findByOwner(user).isEmpty()) {
            log.warn("Cannot delete userId={} — active bank accounts exist", userId);
            throw new IllegalStateException("User has active bank accounts");
        }

        // 4️⃣ Delete user
        userRepository.deleteById(userId);
        log.info("User deleted successfully: userId={} by caller={}", userId, authenticatedUserId);
    }
}
