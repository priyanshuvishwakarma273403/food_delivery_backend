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
        String otp = String.format("%06d", random.nextInt(1000000));
        otpCache.put(email, otp);
        expirationCache.put(email, System.currentTimeMillis() + EXPIRATION_TIME);
        log.info("Generated OTP for {}: {}", email, otp);
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        if (!otpCache.containsKey(email)) return false;
        
        long expiry = expirationCache.getOrDefault(email, 0L);
        if (System.currentTimeMillis() > expiry) {
            otpCache.remove(email);
            expirationCache.remove(email);
            return false;
        }

        boolean isValid = otpCache.get(email).equals(otp);
        if (isValid) {
            otpCache.remove(email);
            expirationCache.remove(email);
        }
        return isValid;
    }
}
