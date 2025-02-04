package com.server.app.service;

import com.server.app.dto.UserSessionDto;
import com.server.app.model.User;
import com.server.app.model.UserSession;
import com.server.app.repository.UserSessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserSessionService {

    @Autowired
    private UserSessionRepository userSessionRepository;

    @Transactional
    public UserSessionDto upsertSession(UUID userId, String token, String ipAddress, String userAgent, LocalDateTime expiresAt) {
        // Deactivate any existing active session for the user
        Optional<UserSession> existingSession = userSessionRepository.findByUser_IdAndIsActiveTrue(userId);
        existingSession.ifPresent(session -> {
            session.setActive(false);
            userSessionRepository.save(session);
        });

        // Create new session
        UserSession newSession = UserSession.builder()
                .user(User.builder().id(userId).build())
                .token(token)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .createdAt(LocalDateTime.now())
                .lastAccessedAt(LocalDateTime.now())
                .expiresAt(expiresAt)
                .isActive(true)
                .build();

        UserSession savedSession = userSessionRepository.save(newSession);
        return convertToDto(savedSession);
    }

    public Optional<UserSessionDto> findByToken(String token) {
        return userSessionRepository.findByToken(token)
                .map(this::convertToDto);
    }

    public Optional<UserSessionDto> findActiveSessionByUserId(UUID userId) {
        return userSessionRepository.findByUser_IdAndIsActiveTrue(userId)
                .map(this::convertToDto);
    }

    private UserSessionDto convertToDto(UserSession session) {
        return UserSessionDto.builder()
                .id(session.getId())
                .userId(session.getUser().getId())
                .token(session.getToken())
                .ipAddress(session.getIpAddress())
                .userAgent(session.getUserAgent())
                .createdAt(session.getCreatedAt())
                .lastAccessedAt(session.getLastAccessedAt())
                .expiresAt(session.getExpiresAt())
                .isActive(session.isActive())
                .build();
    }

    @Transactional
    public boolean deactivateSession(String token, UUID userId) {
        Optional<UserSession> session = userSessionRepository.findByToken(token);
        if (session.isPresent() && session.get().getUser().getId().equals(userId)) {
            UserSession userSession = session.get();
            userSession.setActive(false);
            userSession.setExpiresAt(LocalDateTime.now());
            userSessionRepository.save(userSession);
            return true;
        }
        return false;
    }
}