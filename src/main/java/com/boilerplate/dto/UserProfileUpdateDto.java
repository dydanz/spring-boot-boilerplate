package com.boilerplate.dto;

import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserProfileUpdateDto {
    private String fullName;
    private String photoUrl;
    
    @Past(message = "Birth date must be in the past")
    private LocalDate birthDate;
    
    private String phoneNumber;
} 