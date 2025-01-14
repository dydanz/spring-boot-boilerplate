package com.boilerplate.service;

import com.boilerplate.dto.AuthResponseDto;
import com.boilerplate.dto.LoginRequestDto;
import com.boilerplate.dto.UserRegistrationDto;
import com.boilerplate.entity.User;
import com.boilerplate.repository.UserRepository;
import com.boilerplate.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenCacheService tokenCacheService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponseDto register(UserRegistrationDto request) {
        User user = userService.registerUser(request);
        String token = jwtService.generateToken(createUserDetails(user));
        tokenCacheService.cacheToken(user.getEmail(), token, 28800);
        return createAuthResponse(user, token);
    }

    public AuthResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(createUserDetails(user));
        tokenCacheService.cacheToken(user.getEmail(), token, 28800);
        return createAuthResponse(user, token);
    }

    public AuthResponseDto verifyOtp(String email, String otp) {
        String cachedOtp = tokenCacheService.getOtp(email);
        if (cachedOtp == null || !cachedOtp.equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEnabled(true);
        userRepository.save(user);

        String token = jwtService.generateToken(createUserDetails(user));
        tokenCacheService.cacheToken(user.getEmail(), token, 28800);
        return createAuthResponse(user, token);
    }

    public AuthResponseDto refreshToken(String token) {
        String email = jwtService.extractUsername(token.substring(7));
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        String newToken = jwtService.generateToken(createUserDetails(user));
        tokenCacheService.cacheToken(user.getEmail(), newToken, 28800);
        return createAuthResponse(user, newToken);
    }

    public void logout(String token) {
        String email = jwtService.extractUsername(token.substring(7));
        tokenCacheService.invalidateToken(email);
    }

    private UserDetails createUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getEmail())
            .password(user.getPassword())
            .roles("USER")
            .build();
    }

    private AuthResponseDto createAuthResponse(User user, String token) {
        return AuthResponseDto.builder()
            .token(token)
            .email(user.getEmail())
            .build();
    }
} 