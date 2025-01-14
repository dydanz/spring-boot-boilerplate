package com.boilerplate.service;

import com.boilerplate.dto.UserRegistrationDto;
import com.boilerplate.entity.User;
import com.boilerplate.repository.UserProfileRepository;
import com.boilerplate.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailService emailService;
    @Mock
    private KafkaEventService kafkaEventService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, userProfileRepository, 
            passwordEncoder, null, emailService, kafkaEventService);
    }

    @Test
    void whenRegisterUser_thenSuccess() {
        // Given
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("test@example.com");
        dto.setPassword("password");
        dto.setFullName("Test User");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userRepository.save(any())).thenReturn(new User());

        // When
        User result = userService.registerUser(dto);

        // Then
        assertNotNull(result);
    }
} 