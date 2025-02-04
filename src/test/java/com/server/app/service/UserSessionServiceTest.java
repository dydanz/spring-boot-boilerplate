package com.server.app.service;

import com.server.app.dto.UserSessionDto;
import com.server.app.model.User;
import com.server.app.model.UserSession;
import com.server.app.repository.UserSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class UserSessionServiceTest {

    @Mock
    private UserSessionRepository userSessionRepository;

    @InjectMocks
    private UserSessionService userSessionService;

    private UUID userId;
    private String token;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime expiresAt;
    private UserSession existingSession;
    private UserSession newSession;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        token = "test-token";
        ipAddress = "127.0.0.1";
        userAgent = "Mozilla/5.0";
        expiresAt = LocalDateTime.now().plusDays(1);

        User user = User.builder().id(userId).build();

        existingSession = UserSession.builder()
                .user(user)
                .token("old-token")
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .isActive(true)
                .build();

        newSession = UserSession.builder()
                .user(user)
                .token(token)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .isActive(true)
                .build();
    }

    @Test
    void upsertSession_WhenExistingActiveSession_ShouldDeactivateAndCreateNew() {
        // Arrange
        when(userSessionRepository.findByUser_IdAndIsActiveTrue(userId))
                .thenReturn(Optional.of(existingSession));
        when(userSessionRepository.save(any(UserSession.class)))
                .thenReturn(existingSession)
                .thenReturn(newSession);

        // Act
        UserSessionDto result = userSessionService.upsertSession(userId, token, ipAddress, userAgent, expiresAt);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(token, result.getToken());
        assertEquals(ipAddress, result.getIpAddress());
        assertEquals(userAgent, result.getUserAgent());
        assertTrue(result.isActive());
        
        verify(userSessionRepository).findByUser_IdAndIsActiveTrue(userId);
        verify(userSessionRepository, times(2)).save(any(UserSession.class));
    }

    @Test
    void upsertSession_WhenNoExistingActiveSession_ShouldCreateNew() {
        // Arrange
        when(userSessionRepository.findByUser_IdAndIsActiveTrue(userId))
                .thenReturn(Optional.empty());
        when(userSessionRepository.save(any(UserSession.class)))
                .thenReturn(newSession);

        // Act
        UserSessionDto result = userSessionService.upsertSession(userId, token, ipAddress, userAgent, expiresAt);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(token, result.getToken());
        verify(userSessionRepository).findByUser_IdAndIsActiveTrue(userId);
        verify(userSessionRepository, times(1)).save(any(UserSession.class));
    }

    @Test
    void findByToken_WhenTokenExists_ShouldReturnSession() {
        // Arrange
        when(userSessionRepository.findByToken(token))
                .thenReturn(Optional.of(newSession));

        // Act
        Optional<UserSessionDto> result = userSessionService.findByToken(token);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(token, result.get().getToken());
        verify(userSessionRepository).findByToken(token);
    }

    @Test
    void findByToken_WhenTokenDoesNotExist_ShouldReturnEmpty() {
        // Arrange
        when(userSessionRepository.findByToken(token))
                .thenReturn(Optional.empty());

        // Act
        Optional<UserSessionDto> result = userSessionService.findByToken(token);

        // Assert
        assertFalse(result.isPresent());
        verify(userSessionRepository).findByToken(token);
    }

    @Test
    void findActiveSessionByUserId_WhenActiveSessionExists_ShouldReturnSession() {
        // Arrange
        when(userSessionRepository.findByUser_IdAndIsActiveTrue(userId))
                .thenReturn(Optional.of(newSession));

        // Act
        Optional<UserSessionDto> result = userSessionService.findActiveSessionByUserId(userId);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getUserId());
        assertTrue(result.get().isActive());
        verify(userSessionRepository).findByUser_IdAndIsActiveTrue(userId);
    }

    @Test
    void findActiveSessionByUserId_WhenNoActiveSession_ShouldReturnEmpty() {
        // Arrange
        when(userSessionRepository.findByUser_IdAndIsActiveTrue(userId))
                .thenReturn(Optional.empty());

        // Act
        Optional<UserSessionDto> result = userSessionService.findActiveSessionByUserId(userId);

        // Assert
        assertFalse(result.isPresent());
        verify(userSessionRepository).findByUser_IdAndIsActiveTrue(userId);
    }

    @Test
    void deactivateSession_WhenValidTokenAndUserId_ShouldDeactivateAndReturnTrue() {
        // Arrange
        when(userSessionRepository.findByToken(token))
                .thenReturn(Optional.of(newSession));
        when(userSessionRepository.save(any(UserSession.class)))
                .thenReturn(newSession);

        // Act
        boolean result = userSessionService.deactivateSession(token, userId);

        // Assert
        assertTrue(result);
        assertFalse(newSession.isActive());
        assertNotNull(newSession.getExpiresAt());
        verify(userSessionRepository).findByToken(token);
        verify(userSessionRepository).save(newSession);
    }

    @Test
    void deactivateSession_WhenInvalidToken_ShouldReturnFalse() {
        // Arrange
        when(userSessionRepository.findByToken(token))
                .thenReturn(Optional.empty());

        // Act
        boolean result = userSessionService.deactivateSession(token, userId);

        // Assert
        assertFalse(result);
        verify(userSessionRepository).findByToken(token);
        verify(userSessionRepository, never()).save(any());
    }

    @Test
    void deactivateSession_WhenUserIdMismatch_ShouldReturnFalse() {
        // Arrange
        UUID differentUserId = UUID.randomUUID();
        when(userSessionRepository.findByToken(token))
                .thenReturn(Optional.of(newSession));

        // Act
        boolean result = userSessionService.deactivateSession(token, differentUserId);

        // Assert
        assertFalse(result);
        verify(userSessionRepository).findByToken(token);
        verify(userSessionRepository, never()).save(any());
    }
}