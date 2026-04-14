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
            jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = 
                new org.springframework.mail.javamail.MimeMessageHelper(mimeMessage, "utf-8");

            String htmlMsg = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; border: 1px solid #eee; padding: 20px; border-radius: 10px;'>" +
                    "<div style='text-align: center;'><h1 style='color: #e23744;'>TOMATO</h1></div>" +
                    "<h2 style='color: #333; text-align: center;'>Verify Your Account</h2>" +
                    "<p style='color: #666; font-size: 16px; text-align: center;'>Use the following code to complete your registration on Tomato.</p>" +
                    "<div style='background: #fdf2f2; padding: 20px; text-align: center; border-radius: 8px; margin: 20px 0;'>" +
                    "<span style='font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #e23744;'>" + otp + "</span>" +
                    "</div>" +
                    "<p style='color: #999; font-size: 12px; text-align: center;'>This code will expire in 5 minutes. If you didn't request this, please ignore this email.</p>" +
                    "<hr style='border: 0; border-top: 1px solid #eee; margin: 20px 0;'>" +
                    "<p style='color: #ccc; font-size: 10px; text-align: center;'>&copy; 2026 Tomato Food Delivery. Made with ❤️ for food lovers.</p>" +
                    "</div>";

            helper.setText(htmlMsg, true); // true indicates HTML
            helper.setTo(to);
            helper.setSubject("Verify Your Tomato Account - Code: " + otp);
            helper.setFrom("Tomato <noreply@tomato.app>");

            mailSender.send(mimeMessage);
            log.info("Professional HTML email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send HTML email to {}: {}", to, e.getMessage());
            log.warn("\n\n************************************\n  FALLBACK OTP FOR {}: {}\n************************************\n", to, otp);
        }
    }
}
