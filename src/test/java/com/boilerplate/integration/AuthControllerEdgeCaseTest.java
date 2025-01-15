package com.boilerplate.integration;

import com.boilerplate.dto.LoginRequestDto;
import com.boilerplate.dto.UserRegistrationDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void whenRegisterWithExistingEmail_thenConflict() throws Exception {
        UserRegistrationDto request = new UserRegistrationDto();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void whenLoginWithInvalidCredentials_thenUnauthorized() throws Exception {
        LoginRequestDto request = new LoginRequestDto();
        request.setEmail("nonexistent@example.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenVerifyOtpWithInvalidToken_thenBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/auth/verify-otp")
                .param("email", "test@example.com")
                .param("otp", "invalid-otp"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void whenRefreshTokenWithExpiredToken_thenUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh-token")
                .header("Authorization", "Bearer expired-token"))
                .andExpect(status().isUnauthorized());
    }
} 