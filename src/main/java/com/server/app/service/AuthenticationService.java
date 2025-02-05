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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException ;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

    private final RedisService redisService;

    @Value("${app.security.jwt.expiration}")
    private long jwtExpiration;

    private final UserRepository userRepository;
    private final OtpVerificationRepository otpVerificationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserSessionService userSessionService;
    // private final EmailService emailService;

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return AuthenticationResponse.builder()
                    .message("Email already registered")
                    .success(false)
                    .build();
        }

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        user = userRepository.save(user);
        String otp = generateOtp();
        createOtpVerification(user, otp);
        // emailService.sendOtpEmail(user.getEmail(), otp);

        return AuthenticationResponse.builder()
                .message("Registration successful. Please verify your email with the OTP sent.")
                .success(true)
                .build();
    }

    @Transactional
    public AuthenticationResponse verifyOtp(VerifyOtpRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        if (user == null) {
            log.error("OTP verification failed: User not found for email: {}", request.getEmail());
            return AuthenticationResponse.builder()
                    .message("User not found")
                    .success(false)
                    .build();
        }

        var otpVerification = otpVerificationRepository.findLatestByUser(user)
                .orElse(null);
        if (otpVerification == null) {
            log.error("OTP verification failed: No OTP verification found for user: {}", user.getEmail());
            return AuthenticationResponse.builder()
                    .message("No OTP verification found")
                    .success(false)
                    .build();
        }

        if (otpVerification.isUsed()) {
            log.error("OTP verification failed: OTP already used for user: {}", user.getEmail());
            return AuthenticationResponse.builder()
                    .message("OTP already used")
                    .success(false)
                    .build();
        }

        if (otpVerification.getExpiryTime().isBefore(LocalDateTime.now())) {
            log.error("OTP verification failed: OTP expired for user: {}", user.getEmail());
            return AuthenticationResponse.builder()
                    .message("OTP expired")
                    .success(false)
                    .build();
        }

        if (!otpVerification.getOtp().equals(request.getOtp())) {
            otpVerification.setAttempts(otpVerification.getAttempts() + 1);
            otpVerificationRepository.save(otpVerification);
            log.error("OTP verification failed: Invalid OTP for user: {}, attempt: {}", user.getEmail(), otpVerification.getAttempts());
            return AuthenticationResponse.builder()
                    .message("Invalid OTP")
                    .success(false)
                    .build();
        }

        user.setVerified(true);
        userRepository.save(user);
        otpVerification.setUsed(true);
        otpVerificationRepository.save(otpVerification);

        var jwtToken = jwtService.generateToken(user);
        log.info("OTP verification successful for user: {}", user.getEmail());
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .message("Email verified successfully")
                .success(true)
                .build();
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request, String ipAddress, String userAgent) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            var user = userRepository.findByEmail(request.getEmail())
                    .orElse(null);
            if (user == null) {
                log.error("Authentication failed: User not found for email: {}", request.getEmail());
                return AuthenticationResponse.builder()
                        .message("Invalid credentials")
                        .success(false)
                        .build();
            }

            if (!user.isVerified()) {
                log.error("Authentication failed: Unverified user attempt to login: {}", user.getEmail());
                return AuthenticationResponse.builder()
                        .message("Please verify your email first")
                        .success(false)
                        .build();
            }

            var jwtToken = jwtService.generateToken(user);
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Create user session
            LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(jwtExpiration); // Token expires in 1 day
            userSessionService.upsertSession(user.getId(), jwtToken, ipAddress, userAgent, expiresAt);

            // Cache user object in Redis
            redisService.set(user.getEmail(), user, jwtExpiration);

            log.info("Authentication successful for user: {}", user.getEmail());
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .message("Authentication successful")
                    .success(true)
                    .build();
        } catch (BadCredentialsException e) {
            log.error("Authentication failed: Invalid credentials for email: {}", request.getEmail());
            return AuthenticationResponse.builder()
                    .message("Invalid credentials")
                    .success(false)
                    .build();
        }
    }

    public AuthenticationResponse logout(String token, String ipAddress, String userAgent) {
        var user = jwtService.extractUser(token);
        if (user == null) {
            log.error("Logout failed: Invalid token");
            return AuthenticationResponse.builder()
                    .message("Invalid session")
                    .success(false)
                    .build();
        }

        boolean sessionDeactivated = userSessionService.deactivateSession(token, user.getId());
        
        if (!sessionDeactivated) {
            log.error("Logout failed: Invalid session or user mismatch for userId: {}", user.getId());
            return AuthenticationResponse.builder()
                    .message("Invalid session")
                    .success(false)
                    .build();
        }

        // Remove user object from Redis cache
        redisService.delete(user.getEmail());

        log.info("User successfully logged out, userId: {}", user.getId());
        return AuthenticationResponse.builder()
                .message("Logged out successfully")
                .success(true)
                .build();
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    private void createOtpVerification(User user, String otp) {
        var otpVerification = OtpVerification.builder()
                .user(user)
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();

        otpVerificationRepository.save(otpVerification);
    }

    @Transactional
    public AuthenticationResponse resetOtp(String email) {
        var user = userRepository.findByEmail(email)
                .orElse(null);
        if (user == null) {
            log.error("OTP reset failed: User not found for email: {}", email);
            return AuthenticationResponse.builder()
                    .message("User not found")
                    .success(false)
                    .build();
        }

        var otpVerification = otpVerificationRepository.findLatestByUser(user)
                .orElse(null);
        if (otpVerification == null) {
            log.error("OTP reset failed: No OTP verification found for user: {}", user.getEmail());
            return AuthenticationResponse.builder()
                    .message("No OTP verification found")
                    .success(false)
                    .build();
        }

        String newOtp = generateOtp();
        otpVerification.setOtp(newOtp);
        otpVerification.setExpiryTime(LocalDateTime.now().plusMinutes(5));
        otpVerification.setAttempts(0);
        otpVerification.setUsed(false);
        otpVerificationRepository.save(otpVerification);

        // emailService.sendOtpEmail(user.getEmail(), newOtp);

        log.info("OTP reset successful for user: {}", user.getEmail());
        return AuthenticationResponse.builder()
                .message("OTP has been reset and sent to your email")
                .success(true)
                .build();
    }
}