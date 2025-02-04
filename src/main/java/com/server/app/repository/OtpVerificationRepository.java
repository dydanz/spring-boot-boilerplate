package com.server.app.repository;

import com.server.app.model.OtpVerification;
import com.server.app.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    @Query("SELECT o FROM OtpVerification o WHERE o.user = ?1 ORDER BY o.createdAt DESC")
    Optional<OtpVerification> findLatestByUser(User user);
} 