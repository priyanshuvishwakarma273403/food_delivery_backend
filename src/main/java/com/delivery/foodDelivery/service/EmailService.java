package com.delivery.foodDelivery.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendVerificationEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("Verify Your Tomato Account");
            message.setText("Account Verification Code: " + otp + "\n\n" +
                            "This code will expire in 5 minutes.\n\n" +
                            "Thank you,\nTeam Tomato");
            
            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // In development, we still want to know the OTP if email fails
            log.warn("FALLBACK OTP for {}: {}", to, otp);
        }
    }
}
