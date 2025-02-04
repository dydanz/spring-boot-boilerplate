package com.server.app.controller;

import com.server.app.dto.AuthenticationRequest;
import com.server.app.dto.AuthenticationResponse;
import com.server.app.dto.RegisterRequest;
import com.server.app.dto.VerifyOtpRequest;
import com.server.app.dto.ResetOtpRequest;
import com.server.app.service.AuthenticationService;
import io.github.bucket4j.Bucket;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final Bucket bucket;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        if (!bucket.tryConsume(1)) {
            throw new RuntimeException("Too many registration attempts. Please try again later.");
        }
        if(request.getEmail() == null || request.getEmail().isEmpty() 
        || request.getPassword() == null || request.getPassword().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AuthenticationResponse.builder()
                  .message("Email and Password is required")
                  .success(false)
                  .build());
        }
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthenticationResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request
    ) {
        if (!bucket.tryConsume(1)) {
            throw new RuntimeException("Too many OTP verification attempts. Please try again later.");
        }

        if(request.getEmail() == null || request.getEmail().isEmpty() || 
        request.getOtp() == null || request.getOtp().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(AuthenticationResponse.builder()
                   .message("Email and OTP Code is required")
                   .success(false)
                   .build());
        }
        return ResponseEntity.ok(authenticationService.verifyOtp(request));
    }

    @PostMapping("/reset-otp")
    public ResponseEntity<AuthenticationResponse> resetOtp(
            @Valid @RequestBody ResetOtpRequest request
    ) {
        if (!bucket.tryConsume(1)) {
            throw new RuntimeException("Too many OTP Reset attempts. Please try again later.");
        }

        return ResponseEntity.ok(authenticationService.resetOtp(request.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @Valid @RequestBody AuthenticationRequest request,
            HttpServletRequest httpRequest
    ) {
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        return ResponseEntity.ok(authenticationService.authenticate(request, ipAddress, userAgent));
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthenticationResponse> logout(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest
    ) {
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        String ipAddress = httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");
        AuthenticationResponse response = authenticationService.logout(token, ipAddress, userAgent);
        return ResponseEntity.ok(response);
    }
}