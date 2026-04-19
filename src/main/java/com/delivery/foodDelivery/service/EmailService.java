package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.dto.SaleEventDTO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

import com.delivery.foodDelivery.repository.jpa.UserRepository;
import com.delivery.foodDelivery.entity.User;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    
    public void sendVerificationEmail(String toEmail, String otp) {
        try {
            log.info("Sending verification OTP to: {}", toEmail);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Your FoodDelivery Verification OTP");
            helper.setText("<h1>Welcome to FoodDelivery!</h1>" +
                    "<p>Your verification code is: <strong>" + otp + "</strong></p>" +
                    "<p>This code is valid for 5 minutes.</p>", true);

            mailSender.send(message);
            log.info("OTP sent successfully to: {}", toEmail);
        } catch (MessagingException e) {
            log.error("Failed to send OTP to {}: {}", toEmail, e.getMessage());
            throw new RuntimeException("Email sending failed");
        }
    }


    @Async("emailExecutor")
    public void sendSaleNotificationEmail(String toEmail, String userName, SaleEventDTO saleEvent) {
        try {
            log.info("Sending email to: {}", toEmail);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, 
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, 
                    StandardCharsets.UTF_8.name());

            String content = String.format(
                    "<html><body>" +
                    "<h1>Hi %s,</h1>" +
                    "<h2>%s</h2>" +
                    "<p>%s</p>" +
                    "<p>Use Promo Code: <strong>%s</strong> for <strong>%.0f%% OFF!</strong></p>" +
                    "<br><p>Enjoy your meal!</p>" +
                    "</body></html>",
                    userName, saleEvent.getTitle(), saleEvent.getMessage(), 
                    saleEvent.getPromoCode(), saleEvent.getDiscountPercentage()
            );

            helper.setTo(toEmail);
            helper.setSubject("🔥 Flash Sale Started: " + saleEvent.getTitle() + " 🔥");
            helper.setText(content, true);
            helper.setFrom("no-reply@fooddelivery.com");

            mailSender.send(message);
            log.info("Successfully sent email to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            // In a real scenario, you might want to re-throw or handle differently
        }
    }

    /**
     * Sends sale notification to all registered users.
     */
    public void sendBulkSaleEmails(SaleEventDTO saleEvent) {
        log.info("Triggering bulk sale emails for: {}", saleEvent.getTitle());
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                sendSaleNotificationEmail(user.getEmail(), user.getName(), saleEvent);
            }
        }
        log.info("Finished triggering emails for {} users", users.size());
    }
}
