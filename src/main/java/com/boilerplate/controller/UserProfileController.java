package com.boilerplate.controller;

import com.boilerplate.dto.UserProfileUpdateDto;
import com.boilerplate.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;

    @GetMapping
    public ResponseEntity<?> getCurrentUserProfile() {
        return ResponseEntity.ok(userProfileService.getCurrentUserProfile());
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@Valid @RequestBody UserProfileUpdateDto profileDto) {
        return ResponseEntity.ok(userProfileService.updateProfile(profileDto));
    }

    @DeleteMapping
    public ResponseEntity<?> deleteProfile() {
        userProfileService.deleteProfile();
        return ResponseEntity.ok().build();
    }
} 