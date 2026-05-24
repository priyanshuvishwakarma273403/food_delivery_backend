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
            log.info("Attempting to send verification OTP to: {}", toEmail);
            
            // Check if email credentials are configured
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Your FoodDelivery Verification OTP");
            helper.setText("<h1>Welcome to FoodDelivery!</h1>" +
                    "<p>Your verification code is: <strong>" + otp + "</strong></p>" +
                    "<p>This code is valid for 5 minutes.</p>", true);

            mailSender.send(message);
            log.info("OTP sent successfully to: {}", toEmail);
        } catch (Exception e) {
            log.error("****************************************************************");
            log.error("EMAIL SENDING FAILED: {}", e.getMessage());
            log.error("OTP for {}: {}", toEmail, otp);
            log.error("Please configure spring.mail.username/password in application.properties");
            log.error("DEVELOPMENT TIP: Use master OTP '123456' to bypass this.");
            log.error("****************************************************************");
            // We DON'T throw exception here to allow user to proceed in local development
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

    @Async("emailExecutor")
    public void sendOrderUpdateEmail(String toEmail, String userName, Long orderId, String status) {
        try {
            log.info("Sending order update email to: {} for order: {}", toEmail, orderId);
            
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            String title = "";
            String emoji = "";
            String bodyMessage = "";
            
            if ("PLACED".equalsIgnoreCase(status)) {
                title = "Order Confirmed!";
                emoji = "✅";
                bodyMessage = "Great news! Your order #" + orderId + " has been successfully placed. The restaurant is preparing your delicious food.";
            } else if ("OUT_FOR_DELIVERY".equalsIgnoreCase(status)) {
                title = "Order is on the way!";
                emoji = "🛵";
                bodyMessage = "Get ready! Your order #" + orderId + " has been picked up and is on its way to you.";
            } else if ("DELIVERED".equalsIgnoreCase(status)) {
                title = "Order Delivered!";
                emoji = "🍽️";
                bodyMessage = "Your order #" + orderId + " has been delivered safely. Enjoy your meal!";
            } else {
                title = "Order Update: " + status;
                emoji = "🔔";
                bodyMessage = "Your order #" + orderId + " status is now: " + status + ".";
            }

            String content = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<style>" +
                    "  .container { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: auto; border: 1px solid #ddd; border-radius: 12px; overflow: hidden; }" +
                    "  .header { background: #FF5500; color: white; padding: 30px 20px; text-align: center; }" +
                    "  .header h1 { margin: 0; font-size: 28px; }" +
                    "  .content { padding: 30px; background-color: #ffffff; color: #333; line-height: 1.6; text-align: center; }" +
                    "  .emoji { font-size: 48px; margin-bottom: 10px; }" +
                    "  .footer { background-color: #f9f9f9; padding: 20px; text-align: center; font-size: 12px; color: #888; border-top: 1px solid #eee; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "  <div class='container'>" +
                    "    <div class='header'>" +
                    "      <h1>" + emoji + " " + title + "</h1>" +
                    "    </div>" +
                    "    <div class='content'>" +
                    "      <h3>Hello " + userName + "!</h3>" +
                    "      <p>" + bodyMessage + "</p>" +
                    "      <p>Thank you for choosing Tomato Food Delivery.</p>" +
                    "    </div>" +
                    "    <div class='footer'>" +
                    "      <p>&copy; 2026 Tomato Food Delivery. All Rights Reserved.</p>" +
                    "    </div>" +
                    "  </div>" +
                    "</body>" +
                    "</html>";

            helper.setTo(toEmail);
            helper.setSubject("Tomato Food - " + title);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Successfully sent order email to: {}", toEmail);
            
        } catch (MessagingException e) {
            log.error("Failed to send order email to {}: {}", toEmail, e.getMessage());
        }
    }
}
