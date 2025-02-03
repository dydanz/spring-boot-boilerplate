package com.boilerplate.controller;

import com.boilerplate.dto.PasswordResetRequestDto;
import com.boilerplate.dto.PasswordUpdateDto;
import com.boilerplate.service.PasswordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/password")
@RequiredArgsConstructor
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/forgot")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto request) {
        passwordService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    public ResponseEntity<Void> resetPassword(
            @RequestParam String token,
            @Valid @RequestBody PasswordUpdateDto request
    ) {
        passwordService.resetPassword(token, request.getNewPassword());
        return ResponseEntity.ok().build();
    }
} 