package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.entity.Order;
import com.delivery.foodDelivery.repository.jpa.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
        if (groqApiKey == null || groqApiKey.isBlank()) {
            return "I'm your Tomato assistant. How can I help you today?";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", (groqModel != null && !groqModel.isBlank()) ? groqModel : "llama3-8b-8192");
            body.put("messages", List.of(
                Map.of("role", "system", "content", "You are TomatoAI, a professional and helpful food delivery assistant."),
                Map.of("role", "user", "content", prompt)
            ));
            body.put("temperature", 0.7);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            log.info("Calling Groq AI with prompt: {}", prompt);
            
            Map<String, Object> response = restTemplate.postForObject(groqApiUrl, entity, Map.class);

            if (response != null && response.containsKey("choices")) {
                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                    return (String) message.get("content");
                }
            }
        } catch (Exception e) {
            log.error("Groq AI Error Detail: {}", e.getMessage());
            if (e.getMessage().contains("401")) return "AI Error: Invalid API Key. Please check your GROQ_API_KEY in Render.";
            if (e.getMessage().contains("429")) return "AI is busy (Rate limit). Please try again in 1 minute.";
        }
        return "I'm having trouble reaching my brain (Groq AI). Please check if the API key is set correctly in Render environment variables.";

    }
}
