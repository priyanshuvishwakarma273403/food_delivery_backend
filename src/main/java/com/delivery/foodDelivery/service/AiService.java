package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.entity.Order;
import com.delivery.foodDelivery.repository.jpa.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.api.url}")
    private String groqApiUrl;

    @Value("${groq.api.model}")
    private String groqModel;

    public String getSmartSuggestion(Long userId) {
        List<Order> history = orderRepository.findByCustomerIdOrderByCreatedDateDesc(userId);
        
        String historySummary = history.stream()
                .limit(5)
                .map(o -> o.getStatus().name()) // Simplification, ideally items
                .collect(Collectors.joining(", "));

        String prompt = String.format(
            "User has ordered these types of food recently: [%s]. " +
            "Suggest one specific healthy alternative or a popular pairing in one short sentence starting with 'Based on your recent orders...'",
            historySummary
        );

        return callGroq(prompt);
    }

    public String getChatbotResponse(String userMessage) {
        String systemPrompt = "You are TomatoAI, a friendly food delivery assistant. Suggest 3 delicious dishes based on user preference. Keep it under 50 words.";
        return callGroq(systemPrompt + "\nUser: " + userMessage);
    }

    private String callGroq(String prompt) {
        log.info("Attempting to call Groq AI. API Key present: {}", (groqApiKey != null && !groqApiKey.isBlank()));
        
        if (groqApiKey == null || groqApiKey.isBlank()) {
            return "I'm TomatoAI. Please set the GROQ_API_KEY in Render to start chatting!";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + groqApiKey.trim());

            Map<String, Object> body = new HashMap<>();
            // Using the most stable Llama 3 model
            body.put("model", (groqModel != null && !groqModel.isBlank()) ? groqModel : "llama3-70b-8192");
            body.put("messages", List.of(
                Map.of("role", "system", "content", "You are TomatoAI, a friendly food delivery assistant. Give short, helpful answers."),
                Map.of("role", "user", "content", prompt)
            ));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            
            log.info("Sending request to Groq: {}", groqApiUrl);
            ResponseEntity<Map> responseEntity = restTemplate.postForEntity(groqApiUrl, entity, Map.class);
            Map<String, Object> response = responseEntity.getBody();

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
            return "AI Error: Received empty response from Groq.";
        } catch (org.springframework.web.client.HttpStatusCodeException e) {
            log.error("Groq API Http Error: {} - Body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return "AI Connection Error: " + e.getStatusCode();
        } catch (Exception e) {
            log.error("Groq AI General Error: {}", e.getMessage());
            return "AI Technical Glitch: " + e.getMessage();
        }
    }

}
