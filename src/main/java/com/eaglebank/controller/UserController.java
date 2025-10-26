package com.eaglebank.controller;

import com.eaglebank.dto.requests.CreateUserRequest;
import com.eaglebank.exception.ResourceNotFoundException;
import com.eaglebank.exception.UnauthorizedException;
import com.eaglebank.model.User;
import com.eaglebank.security.JwtTokenProvider;
import com.eaglebank.service.UserService;
import com.eaglebank.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/v1")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserRequest req) {
        log.info("Request received to create a new user with email={}", req.getEmail());
        User user = new User();
        user.setName(req.getName());
        user.setEmail(req.getEmail());
        user.setPassword(req.getPassword());

        try {
            user.setAddressJson(objectMapper.writeValueAsString(req.getAddress()));
        } catch (Exception e) {
            log.error("Failed to serialize address for user email={}", req.getEmail(), e);
            user.setAddressJson(null);
        }

        user.setPhoneNumber(req.getPhoneNumber());
        User created = userService.createUser(user);
        log.info("User created successfully with id={} and email={}", created.getId(), created.getEmail());

        return ResponseEntity.created(URI.create("/v1/users/" + created.getId())).body(created);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        log.info("Login attempt for email={}", email);
        try {
            String token = authService.authenticate(email, password);
            log.info("Login successful for email={}", email);
            return ResponseEntity.ok(Map.of("token", token));
        } catch (Exception e) {
            log.warn("Login failed for email={} due to {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUser(@PathVariable String userId, org.springframework.security.core.Authentication auth) {
        String caller = auth.getName();
        log.info("Fetching user details for userId={} by caller={}", userId, caller);

        return userService.findById(userId)
                .map(user -> {
                    if (!user.getId().equals(caller)) {
                        log.warn("Unauthorized access attempt by userId={} on targetUserId={}", caller, userId);
                        return ResponseEntity.status(403).body(Map.of("message", "forbidden"));
                    }
                    log.debug("User details retrieved successfully for userId={}", userId);
                    return ResponseEntity.ok(user);
                })
                .orElseGet(() -> {
                    log.error("User not found for userId={} requested by caller={}", userId, caller);
                    return ResponseEntity.status(404).body(Map.of("message", "User not found"));
                });
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(
            @PathVariable String userId,
            org.springframework.security.core.Authentication auth) {

        String caller = auth.getName();
        log.info("Delete request received for userId={} by caller={}", userId, caller);

        try {
            userService.deleteUser(userId, caller);
            log.info("User deleted successfully: userId={} by caller={}", userId, caller);
            return ResponseEntity.noContent().build();

        } catch (ResourceNotFoundException e) {
            log.error("Delete failed - user not found: userId={} by caller={}", userId, caller, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));

        } catch (UnauthorizedException e) {
            log.warn("Unauthorized delete attempt by caller={} on userId={}", caller, userId);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", e.getMessage()));

        } catch (IllegalStateException e) {
            log.warn("Conflict deleting userId={} (active accounts exist) by caller={}", userId, caller);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));

        } catch (Exception e) {
            log.error("Unexpected error deleting userId={} by caller={}", userId, caller, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
}
