package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.config.KafkaConfig;
import com.delivery.foodDelivery.dto.SaleEventDTO;
import com.delivery.foodDelivery.entity.User;
import com.delivery.foodDelivery.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Listener for Sale Events.
     * When a sale starts, it fetches all active users and triggers email notifications.
     */
    @KafkaListener(topics = KafkaConfig.SALE_TOPIC, groupId = "email-service-group")
    public void consumeSaleEvent(SaleEventDTO saleEvent) {
        log.info("Consumed sale event: {}", saleEvent.getTitle());

        int pageSize = 1000; // Batch size for fetching users
        int pageNumber = 0;
        Page<User> userPage;

        do {
            Pageable pageable = PageRequest.of(pageNumber, pageSize);
            userPage = userRepository.findByActiveTrue(pageable); 


            log.info("Processing batch {} with {} users", pageNumber, userPage.getNumberOfElements());

            userPage.getContent().forEach(user -> {
                if (user.getEmail() != null && !user.getEmail().isEmpty()) {
                    emailService.sendSaleNotificationEmail(user.getEmail(), user.getName(), saleEvent);
                }
            });

            pageNumber++;
        } while (userPage.hasNext());

        log.info("Completed processing all users for sale: {}", saleEvent.getTitle());
    }

    /**
     * DLT Listener for failed events
     */
    @KafkaListener(topics = KafkaConfig.SALE_DLT_TOPIC, groupId = "email-service-dlt-group")
    public void consumeDlt(SaleEventDTO saleEvent) {
        log.error("EVENT MOVED TO DLT: {}", saleEvent);
        // Implement logic to notify admin or retry manually later
    }
}
