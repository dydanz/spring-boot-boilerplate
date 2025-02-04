package com.server.app.security;

import com.server.app.model.User;
import com.server.app.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtService jwtService;

    private User testUser;
    private String testToken;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtService, "secretKey", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);

        testUser = User.builder()
                .email("test@example.com")
                .password("password")
                .firstName("Test")
                .lastName("User")
                .build();
    }

    @Test
    void whenGenerateToken_thenSuccessful() {
        String token = jwtService.generateToken(testUser);
        assertThat(token).isNotNull();
        assertThat(token.split("\\.").length).isEqualTo(3); // Header.Payload.Signature
    }

    @Test
    void whenExtractUsername_thenReturnCorrectUsername() {
        String token = jwtService.generateToken(testUser);
        String extractedUsername = jwtService.extractUsername(token);
        assertThat(extractedUsername).isEqualTo(testUser.getEmail());
    }

    @Test
    void whenValidToken_thenReturnTrue() {
        String token = jwtService.generateToken(testUser);
        boolean isValid = jwtService.isTokenValid(token, testUser);
        assertThat(isValid).isTrue();
    }

    @Test
    void whenExtractUser_thenReturnUser() {
        when(userRepository.findByEmail(testUser.getEmail())).thenReturn(Optional.of(testUser));
        
        String token = jwtService.generateToken(testUser);
        User extractedUser = jwtService.extractUser(token);
        
        assertThat(extractedUser).isNotNull();
        assertThat(extractedUser.getEmail()).isEqualTo(testUser.getEmail());
    }
}