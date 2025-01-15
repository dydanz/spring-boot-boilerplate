package com.boilerplate.service;

import com.boilerplate.dto.UserProfileUpdateDto;
import com.boilerplate.entity.User;
import com.boilerplate.entity.UserProfile;
import com.boilerplate.exception.ResourceNotFoundException;
import com.boilerplate.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final KafkaEventService kafkaEventService;

    public UserProfile getCurrentUserProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userProfileRepository.findByUserEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Profile not found"));
    }

    @Transactional
    public UserProfile updateProfile(UserProfileUpdateDto profileDto) {
        UserProfile profile = getCurrentUserProfile();
        
        profile.setFullName(profileDto.getFullName());
        profile.setPhotoUrl(profileDto.getPhotoUrl());
        profile.setBirthDate(profileDto.getBirthDate());
        profile.setPhoneNumber(profileDto.getPhoneNumber());

        UserProfile updatedProfile = userProfileRepository.save(profile);
        kafkaEventService.trackUserEvent(profile.getUser().getId(), "PROFILE_UPDATED");
        
        return updatedProfile;
    }

    @Transactional
    public void deleteProfile() {
        UserProfile profile = getCurrentUserProfile();
        userProfileRepository.delete(profile);
        kafkaEventService.trackUserEvent(profile.getUser().getId(), "PROFILE_DELETED");
    }
} 