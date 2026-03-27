package com.example.codedockbackend.service;

import com.example.codedockbackend.dto.LoginRequest;
import com.example.codedockbackend.exception.InvalidCredentialsException;
import com.example.codedockbackend.model.User;
import com.example.codedockbackend.repository.UserRepository;
import com.example.codedockbackend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;


    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("Invalid credentials")
                );

        if (!passwordEncoder.matches(
                request.getPassword(), user.getPassword()
        )) {
            throw new InvalidCredentialsException();
        }

        return jwtUtil.generateToken(user.getEmail());
    }

    public void forgotPassword(String email) {

        User user = userRepository.findByEmail(email).orElse(null);


        // Do not reveal user existence
        if (user == null) return;

        String token = UUID.randomUUID().toString();

        System.out.println("This is token abhi" + token);

        user.setResetToken(token);
        user.setResetTokenExpiry(System.currentTimeMillis() + (1000 * 60 * 15)); // 15 min

        userRepository.save(user);


        emailService.sendResetEmail(user.getEmail(), token);
    }

    public void resetPassword(String token, String newPassword) {

        User user = userRepository.findByResetToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (user.getResetTokenExpiry() < System.currentTimeMillis()) {
            throw new RuntimeException("Token expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        userRepository.save(user);
    }
}
