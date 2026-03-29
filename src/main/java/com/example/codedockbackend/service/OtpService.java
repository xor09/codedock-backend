package com.example.codedockbackend.service;

import com.example.codedockbackend.model.User;
import com.example.codedockbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {
    private final UserRepository userRepository;
    private final EmailService emailService;

    public void generateAndSendOtp(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendOtpEmail(email, otp);
    }

    public void verifyOtp(String email, String otp) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getOtp() == null || !user.getOtp().equals(otp))
            throw new RuntimeException("Invalid OTP");

        if (user.getOtpExpiry().isBefore(LocalDateTime.now()))
            throw new RuntimeException("OTP expired");

        user.setVerified(true);
        user.setOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }
}