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

            String content = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<style>" +
                    "  .container { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 12px; overflow: hidden; }" +
                    "  .header { background: linear-gradient(135deg, #FF9933 0%, #FF5500 100%); color: white; padding: 40px 20px; text-align: center; }" +
                    "  .header h1 { margin: 0; font-size: 32px; text-shadow: 2px 2px 4px rgba(0,0,0,0.2); }" +
                    "  .content { padding: 30px; background-color: #ffffff; color: #333; line-height: 1.6; }" +
                    "  .sale-box { background: #FFF8E1; border: 2px dashed #FF9933; border-radius: 8px; padding: 20px; text-align: center; margin: 20px 0; }" +
                    "  .promo-code { font-size: 24px; font-weight: bold; color: #D32F2F; letter-spacing: 2px; }" +
                    "  .discount { font-size: 48px; font-weight: 800; color: #FF5500; margin: 10px 0; }" +
                    "  .btn { display: inline-block; padding: 15px 30px; background-color: #FF5500; color: white !important; text-decoration: none; border-radius: 50px; font-weight: bold; margin-top: 20px; box-shadow: 0 4px 15px rgba(255,85,0,0.3); }" +
                    "  .footer { background-color: #f9f9f9; padding: 20px; text-align: center; font-size: 12px; color: #888; border-top: 1px solid #eee; }" +
                    "  .diya { font-size: 40px; margin-bottom: 10px; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "  <div class='container'>" +
                    "    <div class='header'>" +
                    "      <div class='diya'>🪔✨🪔</div>" +
                    "      <h1>" + saleEvent.getTitle() + "</h1>" +
                    "    </div>" +
                    "    <div class='content'>" +
                    "      <h3>Namaste " + userName + "!</h3>" +
                    "      <p>" + saleEvent.getMessage() + "</p>" +
                    "      <div class='sale-box'>" +
                    "        <p style='margin:0; font-weight:600; color:#555;'>GET AN EXCLUSIVE</p>" +
                    "        <div class='discount'>" + String.format("%.0f%% OFF", saleEvent.getDiscountPercentage()) + "</div>" +
                    "        <p style='margin:5px 0;'>Use Code: <span class='promo-code'>" + saleEvent.getPromoCode() + "</span></p>" +
                    "      </div>" +
                    "      <p style='text-align:center;'>" +
                    "        <a href='https://tomato-food.app' class='btn'>Order Now & Celebrate!</a>" +
                    "      </p>" +
                    "      <p>Wishing you and your family a very Happy and Prosperous Diwali! May your plate always be full of joy and delicious food.</p>" +
                    "    </div>" +
                    "    <div class='footer'>" +
                    "      <p>&copy; 2026 Tomato Food Delivery. All Rights Reserved.</p>" +
                    "      <p>If you have any questions, contact us at support@tomato.com</p>" +
                    "    </div>" +
                    "  </div>" +
                    "</body>" +
                    "</html>";

            helper.setTo(toEmail);
            helper.setSubject("🪔 Special Diwali Offer: " + saleEvent.getTitle() + " 🪔");
            helper.setText(content, true);


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
