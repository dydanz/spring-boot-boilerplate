package com.boilerplate.service;

import com.boilerplate.dto.LoginRequestDto;
import com.boilerplate.entity.User;
import com.boilerplate.repository.UserRepository;
import com.boilerplate.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock private UserService userService;
    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private TokenCacheService tokenCacheService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
            userService, jwtService, userRepository,
            tokenCacheService, authenticationManager, passwordEncoder
        );
    }

    @Test
    void whenLogin_withValidCredentials_thenSuccess() {
        // Given
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password");

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(any())).thenReturn("token");

        // When
        var response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals(request.getEmail(), response.getEmail());
        assertNotNull(response.getToken());
    }
} 