package com.delivery.foodDelivery.controller;

import com.delivery.foodDelivery.dto.request.LoginRequest;
import com.delivery.foodDelivery.dto.request.RegisterRequest;
import com.delivery.foodDelivery.dto.response.ApiResponse;
import com.delivery.foodDelivery.dto.response.AuthResponse;
import com.delivery.foodDelivery.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication endpoints — public (no JWT required).
 *
 * POST /auth/register   → Register a new user
 * POST /auth/login      → Login and receive JWT tokens
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user (CUSTOMER by default).
     * Pass "role": "DELIVERY_PARTNER" or "ADMIN" in the body to set other roles.
     *
     * Sample request:
     * {
     *   "name": "Rahul Sharma",
     *   "email": "rahul@example.com",
     *   "password": "secret123",
     *   "phone": "9876543210",
     *   "address": "221B Baker Street, Delhi"
     * }
     */
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@RequestParam String email) {
        authService.sendVerificationOtp(email);
        return ResponseEntity.ok(ApiResponse.success("Verification OTP sent to " + email, null));
    }

    /**
     * Register a new user with OTP verification.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request, request.getOtp());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    /**
     * Login with email + password.
     *
     * Sample request:
     * { "email": "rahul@example.com", "password": "secret123" }
     *
     * Sample response:
     * { "accessToken": "eyJ...", "tokenType": "Bearer", "expiresIn": 86400000 }
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }
}
