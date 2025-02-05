package com.server.app.controller;

import com.server.app.dto.*;
import com.server.app.service.AuthenticationService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private Bucket bucket;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private AuthenticationController authenticationController;

    private RegisterRequest registerRequest;
    private VerifyOtpRequest verifyOtpRequest;
    private AuthenticationRequest authenticationRequest;
    private LogoutRequest logoutRequest;
    private ResetOtpRequest resetOtpRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("password123")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .build();

        verifyOtpRequest = new VerifyOtpRequest("test@example.com", "123456");
        authenticationRequest = new AuthenticationRequest("test@example.com", "password123");
        logoutRequest = new LogoutRequest();
        resetOtpRequest = new ResetOtpRequest("test@example.com");
    }

    @Test
    void register_Success() {
        when(bucket.tryConsume(1)).thenReturn(true);
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenReturn(AuthenticationResponse.builder()
                        .success(true)
                        .message("Registration successful")
                        .build());

        ResponseEntity<AuthenticationResponse> response = 
                authenticationController.register(registerRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Registration successful", response.getBody().getMessage());
    }

    @Test
    void register_RateLimitExceeded() {
        when(bucket.tryConsume(1)).thenReturn(false);

        ResponseEntity<AuthenticationResponse> response = 
                authenticationController.register(registerRequest);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void register_MissingCredentials() {
        when(bucket.tryConsume(1)).thenReturn(true);
        RegisterRequest invalidRequest = RegisterRequest.builder().build();

        ResponseEntity<AuthenticationResponse> response = 
                authenticationController.register(invalidRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Email and Password is required", response.getBody().getMessage());
    }

    @Test
    void register_ServiceException() {
        when(bucket.tryConsume(1)).thenReturn(true);
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        ResponseEntity<AuthenticationResponse> response = 
                authenticationController.register(registerRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Something went wrong on our end. We're working on it—please try again later.", response.getBody().getMessage());
    }

    @Test
    void verifyOtp_MissingFields() {
        when(bucket.tryConsume(1)).thenReturn(true);
        VerifyOtpRequest invalidRequest = new VerifyOtpRequest(null, null);

        ResponseEntity<AuthenticationResponse> response = 
                authenticationController.verifyOtp(invalidRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Email and OTP Code is required", response.getBody().getMessage());
    }

    @Test
    void verifyOtp_ServiceException() {
        when(bucket.tryConsume(1)).thenReturn(true);
        when(authenticationService.verifyOtp(any(VerifyOtpRequest.class)))
                .thenThrow(new RuntimeException("Invalid OTP"));

        ResponseEntity<AuthenticationResponse> response = 
                authenticationController.verifyOtp(verifyOtpRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Something went wrong on our end. We're working on it—please try again later.", response.getBody().getMessage());
    }

    @Test
    void verifyOtp_Success() {
        when(bucket.tryConsume(1)).thenReturn(true);
        when(authenticationService.verifyOtp(any(VerifyOtpRequest.class)))
                .thenReturn(AuthenticationResponse.builder()
                        .success(true)
                        .message("OTP verified successfully")
                        .build());

        ResponseEntity<AuthenticationResponse> response = 
                authenticationController.verifyOtp(verifyOtpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    void verifyOtp_RateLimitExceeded() {
        when(bucket.tryConsume(1)).thenReturn(false);

        ResponseEntity<AuthenticationResponse> response = 
                authenticationController.verifyOtp(verifyOtpRequest);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
    }

    @Test
    void resetOtp_Success() {
        when(bucket.tryConsume(1)).thenReturn(true);
        when(authenticationService.resetOtp(any()))
                .thenReturn(AuthenticationResponse.builder()
                        .success(true)
                        .message("OTP reset successful")
                        .build());

        ResponseEntity<AuthenticationResponse> response = 
                authenticationController.resetOtp(resetOtpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    void authenticate_Success() {
        when(bucket.tryConsume(1)).thenReturn(true);
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("Mozilla");
        when(authenticationService.authenticate(any(), eq("127.0.0.1"), eq("Mozilla")))
                .thenReturn(AuthenticationResponse.builder()
                        .success(true)
                        .message("Authentication successful")
                        .token("jwt.token.here")
                        .build());

        ResponseEntity<AuthenticationResponse> response = 
                authenticationController.authenticate(authenticationRequest, httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertNotNull(response.getBody().getToken());
    }

    @Test
    void logout_Success() {
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("Mozilla");
        when(authenticationService.logout(eq("token"), eq("127.0.0.1"), eq("Mozilla")))
                .thenReturn(AuthenticationResponse.builder()
                        .success(true)
                        .message("Logged out successfully")
                        .build());

        ResponseEntity<AuthenticationResponse> response = 
                authenticationController.logout(logoutRequest, "Bearer token", httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Logged out successfully", response.getBody().getMessage());
    }

    @Test
    void logout_Failed() {
        when(httpRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        when(httpRequest.getHeader("User-Agent")).thenReturn("Mozilla");
        when(authenticationService.logout(eq("token"), eq("127.0.0.1"), eq("Mozilla")))
                .thenReturn(AuthenticationResponse.builder()
                        .success(false)
                        .message("Logout failed: Invalid session")
                        .build());

        ResponseEntity<AuthenticationResponse> response = 
                authenticationController.logout(logoutRequest, "Bearer token", httpRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isSuccess());
        assertEquals("Logout failed: Invalid session", response.getBody().getMessage());
    }
}