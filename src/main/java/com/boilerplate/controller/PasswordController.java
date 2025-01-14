package com.boilerplate.controller;

import com.boilerplate.dto.PasswordResetRequestDto;
import com.boilerplate.dto.PasswordUpdateDto;
import com.boilerplate.service.PasswordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/password")
@RequiredArgsConstructor
@Tag(name = "Password", description = "Password management APIs")
public class PasswordController {

    private final PasswordService passwordService;

    @PostMapping("/forgot")
    @Operation(summary = "Request password reset")
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody PasswordResetRequestDto request) {
        passwordService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset")
    @Operation(summary = "Reset password using token")
    public ResponseEntity<Void> resetPassword(
            @RequestParam String token,
            @Valid @RequestBody PasswordUpdateDto request
    ) {
        passwordService.resetPassword(token, request.getNewPassword());
        return ResponseEntity.ok().build();
    }
} 