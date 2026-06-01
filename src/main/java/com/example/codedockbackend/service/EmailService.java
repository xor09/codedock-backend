package com.example.codedockbackend.service;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendResetEmail(String toEmail, String token) {

        String resetLink = frontendUrl + "/reset-password?token=" + token;

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

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Verify Your Email - CodeDock");
        message.setText(
                "Your verification code is:\n\n"
                        + otp +
                        "\n\nThis code will expire in 10 minutes.\n" +
                        "If you didn't create an account, ignore this email."
        );
        mailSender.send(message);
    }
}
