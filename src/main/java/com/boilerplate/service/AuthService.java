package com.boilerplate.service;

import com.boilerplate.dto.AuthResponseDto;
import com.boilerplate.dto.LoginRequestDto;
import com.boilerplate.dto.UserRegistrationDto;
import com.boilerplate.entity.User;
import com.boilerplate.repository.UserRepository;
import com.boilerplate.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class AuthService implements AuthenticationManager {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final TokenCacheService tokenCacheService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponseDto register(UserRegistrationDto request) {
        User user;

        String token = null;
        try {
            user = userService.registerUser(request);
        } catch (Exception e) {
            logger.info("Unable to create new User");
            throw new RuntimeException(e);
        }

        return createAuthResponse(user, token);
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        User user = userRepository.findByEmail(authentication.getPrincipal().toString())
                .orElseThrow(() -> new BadCredentialsException("User not found"));

        if (passwordEncoder.matches(authentication.getCredentials().toString(), user.getPassword())) {
            return new UsernamePasswordAuthenticationToken(authentication.getPrincipal(),
                    authentication.getCredentials(), new ArrayList<>());
        } else {
            throw new BadCredentialsException("Wrong Password");
        }
    }

    public AuthResponseDto login(LoginRequestDto request) {

        // filter request here
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token;
        try {
            this.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (AuthenticationException e) {
            throw new RuntimeException(e);
        }

        try {
            token = jwtService.generateToken(createUserDetails(user));
        } catch (Exception e) {
            logger.info("Unable to generate token");
            throw new RuntimeException(e);
        }

        updateUserToken(user, token);
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
        user.setTokenExpiryDate(LocalDateTime.now());
        userRepository.save(user);

        return createAuthResponse(user, null);
    }

    public AuthResponseDto refreshToken(String token) {
        String email = jwtService.extractUsername(token.substring(7));
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newToken = jwtService.generateToken(createUserDetails(user));
        updateUserToken(user, token);
        return createAuthResponse(user, newToken);
    }

    public void logout(String token) {
        String email = jwtService.extractUsername(token.substring(7));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setTokenExpiryDate(LocalDateTime.now());
        userRepository.save(user);
        tokenCacheService.invalidateToken(email);
    }

    private void updateUserToken(User user, String newToken) {
        // Set on database
        user.setTokenAuth(newToken);
        user.setTokenExpiryDate(LocalDateTime.now().plusSeconds(28800));
        userRepository.save(user);

        // Update on cache
        tokenCacheService.cacheToken(user.getEmail(), newToken, 28800);
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