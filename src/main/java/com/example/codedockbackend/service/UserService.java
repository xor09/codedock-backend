package com.example.codedockbackend.service;

import com.example.codedockbackend.dto.CreateUserRequest;
import com.example.codedockbackend.service.OtpService;
import com.example.codedockbackend.dto.CreateUserResponse;
import com.example.codedockbackend.model.User;
import com.example.codedockbackend.model.UserRole;
import com.example.codedockbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public CreateUserResponse createUser(CreateUserRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            User existingUser = userRepository.findByEmail(request.getEmail()).get();

            if (!existingUser.isVerified()) {
                // Update password with new one in case user changed it
                existingUser.setPassword(encoder.encode(request.getPassword()));
                existingUser.setName(request.getName()); // update name too
                existingUser.setRole(UserRole.from(request.getRole()));
                userRepository.save(existingUser);
                // Resend fresh OTP for unverified account
                otpService.generateAndSendOtp(request.getEmail());
                throw new RuntimeException("Account exists but not verified. A new OTP has been sent to your email.");
            }

            throw new RuntimeException("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(encoder.encode(request.getPassword()))
                .role(UserRole.from(request.getRole()))
                .isVerified(false)
                .build();

        userRepository.save(user);

        otpService.generateAndSendOtp(request.getEmail());

        return CreateUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}
