package com.example.codedockbackend.service;

import com.example.codedockbackend.dto.LoginRequest;
import com.example.codedockbackend.exception.InvalidCredentialsException;
import com.example.codedockbackend.model.User;
import com.example.codedockbackend.repository.UserRepository;
import com.example.codedockbackend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;


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
}
