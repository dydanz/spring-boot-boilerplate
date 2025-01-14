package com.boilerplate.service;

import com.boilerplate.dto.UserRegistrationDto;
import com.boilerplate.entity.User;
import com.boilerplate.entity.UserProfile;
import com.boilerplate.repository.UserProfileRepository;
import com.boilerplate.repository.UserRepository;
import com.boilerplate.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final EmailService emailService;
    private final KafkaEventService kafkaEventService;

    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        // Check if user exists
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Create user
        User user = new User();
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setEnabled(false);
        User savedUser = userRepository.save(user);

        // Create user profile
        UserProfile profile = new UserProfile();
        profile.setUser(savedUser);
        profile.setFullName(registrationDto.getFullName());
        userProfileRepository.save(profile);

        // Generate and send OTP
        String otp = generateOtp();
        // Store OTP in Redis (implement this)
        try {
            emailService.sendOtpEmail(user.getEmail(), otp);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send OTP email");
        }

        // Track event
        kafkaEventService.trackUserEvent(savedUser.getId(), "USER_REGISTERED");

        return savedUser;
    }

    private String generateOtp() {
        // Implement OTP generation logic
        return String.format("%06d", (int) (Math.random() * 1000000));
    }

    // Add other user-related methods here
} 