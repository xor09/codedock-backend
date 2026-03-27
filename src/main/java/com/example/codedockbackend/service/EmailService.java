package com.example.codedockbackend.service;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    public void sendResetEmail(String toEmail, String token) {

        String resetLink = "http://localhost:5173/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Reset Your Password - CodeDock");
        message.setText(
                "Click the link below to reset your password:\n\n"
                        + resetLink +
                        "\n\nThis link will expire in 15 minutes."
        );

        mailSender.send(message);
    }
}
