package com.server.app.service;

import com.server.app.dto.AuthenticationRequest;
import com.server.app.dto.AuthenticationResponse;
import com.server.app.dto.RegisterRequest;
import com.server.app.dto.VerifyOtpRequest;
import com.server.app.model.OtpVerification;
import com.server.app.model.User;
import com.server.app.repository.OtpVerificationRepository;
import com.server.app.repository.UserRepository;
import com.server.app.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpVerificationRepository otpVerificationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserSessionService userSessionService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "jwtExpiration", 86400L);
    }

    @Test
    void register_WhenEmailNotRegistered_ShouldRegisterSuccessfully() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("test@example.com")
                .password("password")
                .firstName("John")
                .lastName("Doe")
                .phoneNumber("+1234567890")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        AuthenticationResponse response = authenticationService.register(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Registration successful. Please verify your email with the OTP sent.", response.getMessage());
        verify(userRepository).existsByEmail(request.getEmail());
        verify(passwordEncoder).encode(request.getPassword());
        verify(userRepository).save(any(User.class));
        verify(otpVerificationRepository).save(any(OtpVerification.class));
    }

    @Test
    void register_WhenEmailAlreadyRegistered_ShouldReturnError() {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .email("existing@example.com")
                .password("password")
                .build();

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        // Act
        AuthenticationResponse response = authenticationService.register(request);

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Email already registered", response.getMessage());
        verify(userRepository).existsByEmail(request.getEmail());
        verify(userRepository, never()).save(any());
    }

    @Test
    void authenticate_WhenValidCredentials_ShouldAuthenticateSuccessfully() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("test@example.com", "password");
        User user = User.builder()
                .email("test@example.com")
                .verified(true)
                .build();
        String jwtToken = "jwt.token.here";

        when(userRepository.findByEmail(request.getEmail())).thenReturn(java.util.Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn(jwtToken);

        // Act
        AuthenticationResponse response = authenticationService.authenticate(request, "127.0.0.1", "Mozilla");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Authentication successful", response.getMessage());
        assertEquals(jwtToken, response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userSessionService).upsertSession(any(), eq(jwtToken), eq("127.0.0.1"), eq("Mozilla"), any());
    }

    @Test
    void authenticate_WhenInvalidCredentials_ShouldReturnError() {
        // Arrange
        AuthenticationRequest request = new AuthenticationRequest("test@example.com", "wrongpassword");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act
        AuthenticationResponse response = authenticationService.authenticate(request, "127.0.0.1", "Mozilla");

        // Assert
        assertFalse(response.isSuccess());
        assertEquals("Invalid credentials", response.getMessage());
    }

    @Test
    void verifyOtp_WhenValidOtp_ShouldVerifySuccessfully() {
        // Arrange
        User user = User.builder()
                .email("test@example.com")
                .verified(false)
                .build();
        OtpVerification otpVerification = OtpVerification.builder()
                .user(user)
                .otp("123456")
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .used(false)
                .attempts(0)
                .build();
        String jwtToken = "jwt.token.here";

        when(userRepository.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(user));
        when(otpVerificationRepository.findLatestByUser(user)).thenReturn(java.util.Optional.of(otpVerification));
        when(jwtService.generateToken(user)).thenReturn(jwtToken);

        VerifyOtpRequest request = new VerifyOtpRequest("test@example.com", "123456");

        // Act
        AuthenticationResponse response = authenticationService.verifyOtp(request);

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Email verified successfully", response.getMessage());
        assertEquals(jwtToken, response.getToken());
        assertTrue(user.isVerified());
        assertTrue(otpVerification.isUsed());
        verify(userRepository).save(user);
        verify(otpVerificationRepository).save(otpVerification);
    }

    @Test
    void logout_WhenValidToken_ShouldLogoutSuccessfully() {
        // Arrange
        String token = "valid.jwt.token";
        User user = User.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .build();

        when(jwtService.extractUser(token)).thenReturn(user);
        when(userSessionService.deactivateSession(token, user.getId())).thenReturn(true);

        // Act
        AuthenticationResponse response = authenticationService.logout(token, "127.0.0.1", "Mozilla");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("Logged out successfully", response.getMessage());
        verify(userSessionService).deactivateSession(token, user.getId());
    }

    @Test
    void resetOtp_WhenValidEmail_ShouldResetSuccessfully() {
        // Arrange
        User user = User.builder()
                .email("test@example.com")
                .build();
        OtpVerification oldOtpVerification = OtpVerification.builder()
                .user(user)
                .otp("123456")
                .expiryTime(LocalDateTime.now())
                .used(true)
                .attempts(1)
                .build();

        when(userRepository.findByEmail("test@example.com")).thenReturn(java.util.Optional.of(user));
        when(otpVerificationRepository.findLatestByUser(user)).thenReturn(java.util.Optional.of(oldOtpVerification));

        // Act
        AuthenticationResponse response = authenticationService.resetOtp("test@example.com");

        // Assert
        assertTrue(response.isSuccess());
        assertEquals("OTP has been reset and sent to your email", response.getMessage());
        verify(otpVerificationRepository).save(oldOtpVerification);
        assertFalse(oldOtpVerification.isUsed());
        assertEquals(0, oldOtpVerification.getAttempts());
        assertTrue(oldOtpVerification.getExpiryTime().isAfter(LocalDateTime.now()));
    }
}