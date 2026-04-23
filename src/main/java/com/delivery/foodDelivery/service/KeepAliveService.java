package com.delivery.foodDelivery.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
public class KeepAliveService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${server.port:8080}")
    private String port;

    /**
     * Pings the application itself every 10 minutes to prevent Render from sleeping.
     * Render Free Tier spins down after 15 minutes of inactivity.
     */
    @Scheduled(fixedRate = 600000) // 10 minutes in milliseconds
    public void keepServerAlive() {
        try {
            // We ping the root context or a health endpoint
            String url = "http://localhost:" + port + "/api/health/ping";
            log.info("Sending Keep-Alive heartbeat to: {}", url);
            restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            // Even if it fails (e.g. 404), the request itself keeps the server awake
            log.warn("Keep-Alive heartbeat sent, but received an error (This is expected if endpoint is not fully public): {}", e.getMessage());
        }
    }
}
