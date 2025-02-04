package com.server.app.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP for Account Verification");
        message.setText("Your OTP for account verification is: " + otp + "\n\n" +
                "This OTP will expire in 5 minutes.\n" +
                "If you did not request this OTP, please ignore this email.");

        mailSender.send(message);
    }
} 