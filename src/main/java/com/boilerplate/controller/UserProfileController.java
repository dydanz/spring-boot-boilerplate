package com.boilerplate.controller;

import com.boilerplate.dto.UserProfileUpdateDto;
import com.boilerplate.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
@Tag(name = "User Profile", description = "User profile management APIs")
@SecurityRequirement(name = "bearerAuth")
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    @Operation(summary = "Get current user's profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        return ResponseEntity.ok(userProfileService.getCurrentUserProfile());
    }

    @PutMapping
    @Operation(summary = "Update current user's profile")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserProfileUpdateDto profileDto) {
        return ResponseEntity.ok(userProfileService.updateProfile(profileDto));
    }

    @DeleteMapping
    @Operation(summary = "Delete current user's profile")
    public ResponseEntity<?> deleteProfile() {
        userProfileService.deleteProfile();
        return ResponseEntity.ok().build();
    }
} 