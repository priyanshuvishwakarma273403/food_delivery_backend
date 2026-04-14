package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.dto.request.AiChatRequest;
import com.delivery.foodDelivery.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AiService {

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.api.url}")
    private String apiUrl;

    @Value("${groq.api.model}")
    private String model;

    public String getPersonalizedGreeting(String userName) {
        int hour = java.time.LocalTime.now().getHour();
        String timeContext;
        String suggestion;

        if (hour >= 5 && hour < 12) {
            timeContext = "Morning";
            suggestion = "How about a fresh breakfast to kickstart your day?";
        } else if (hour >= 12 && hour < 17) {
            timeContext = "Afternoon";
            suggestion = "Time for a delicious lunch! Have you tried the local favorites?";
        } else if (hour >= 17 && hour < 21) {
            timeContext = "Evening";
            suggestion = "Craving some evening snacks or a hearty dinner?";
        } else {
            timeContext = "Late Night";
            suggestion = "Late night cravings? We have the best desserts and snacks open for you!";
        }

        return String.format("Good %s, %s! %s", timeContext, userName, suggestion);
    }

    public String getChatResponse(AiChatRequest request) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new BusinessException("AI Assistant is not configured on the server.");
        }

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", request.getMessages());
        body.put("temperature", 0.7);
        body.put("max_tokens", 300);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            log.error("Error calling Groq API", e);
            throw new BusinessException("Failed to get response from AI Assistant.");
        }
        return "I couldn't process that. Let me try again!";
    }
}
