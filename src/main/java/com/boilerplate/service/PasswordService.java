package com.boilerplate.service;

import com.boilerplate.entity.User;
import com.boilerplate.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final UserRepository userRepository;
    private final EmailService emailService;
    private final TokenCacheService tokenCacheService;
    private final PasswordEncoder passwordEncoder;

    public void initiatePasswordReset(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String resetToken = generateResetToken();
            tokenCacheService.cacheToken("reset:" + email, resetToken, 900); // 15 minutes
            
            try {
                emailService.sendPasswordResetEmail(email, createResetLink(resetToken));
            } catch (Exception e) {
                throw new RuntimeException("Failed to send password reset email");
            }
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        String email = validateResetToken(token);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenCacheService.invalidateToken("reset:" + email);
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    private String createResetLink(String token) {
        return "https://your-frontend-url/reset-password?token=" + token;
    }

    private String validateResetToken(String token) {
        // Implement token validation logic
        // Return the email associated with the token
        throw new RuntimeException("Not implemented");
    }
} 