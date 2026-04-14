package com.delivery.foodDelivery.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class OtpService {

    private final Map<String, String> otpCache = new ConcurrentHashMap<>();
    private final Random random = new Random();

    // OTP expires in 5 minutes
    private static final long EXPIRATION_TIME = TimeUnit.MINUTES.toMillis(5);
    private final Map<String, Long> expirationCache = new ConcurrentHashMap<>();

    public String generateOtp(String email) {
        String normalizedEmail = email.toLowerCase().trim();
        String otp = String.format("%06d", random.nextInt(1000000));
        
        otpCache.put(normalizedEmail, otp);
        expirationCache.put(normalizedEmail, System.currentTimeMillis() + EXPIRATION_TIME);
        
        log.info("\n\n************************************\n  OTP FOR {}: {}\n************************************\n", normalizedEmail, otp);
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        // Master OTP for development (123456 always works)
        if ("123456".equals(otp)) {
            log.warn("Master OTP 123456 used for email: {}", email);
            return true;
        }

        String normalizedEmail = email.toLowerCase().trim();
        if (!otpCache.containsKey(normalizedEmail)) return false;
        
        long expiry = expirationCache.getOrDefault(normalizedEmail, 0L);
        if (System.currentTimeMillis() > expiry) {
            otpCache.remove(normalizedEmail);
            expirationCache.remove(normalizedEmail);
            log.warn("OTP expired for: {}", normalizedEmail);
            return false;
        }

        boolean isValid = otpCache.get(normalizedEmail).equals(otp);
        if (isValid) {
            otpCache.remove(normalizedEmail);
            expirationCache.remove(normalizedEmail);
        }
        return isValid;
    }
}
