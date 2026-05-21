package com.delivery.foodDelivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableJpaRepositories(basePackages = "com.delivery.foodDelivery.repository.jpa")
@EnableMongoRepositories(basePackages = "com.delivery.foodDelivery.repository.mongo")
@EnableRedisRepositories(basePackages = "com.delivery.foodDelivery.repository.redis")
public class FoodDeliveryApplication {

	public static void main(String[] args) {
		// Robust fix for environment variables with trailing newlines, literal "\n", or quotes
		cleanAndSetProperty("SPRING_DATASOURCE_URL", "spring.datasource.url");
		cleanAndSetProperty("MONGODB_URI", "spring.data.mongodb.uri");
		// cleanAndSetProperty("KAFKA_BOOTSTRAP_SERVERS", "spring.kafka.bootstrap-servers");
		// cleanAndSetProperty("VALKEY_HOST", "spring.data.redis.host");
		// cleanAndSetProperty("ELASTICSEARCH_URL", "spring.elasticsearch.uris");
		// cleanAndSetProperty("ELASTICSEARCH_USERNAME", "spring.elasticsearch.username");
		// cleanAndSetProperty("ELASTICSEARCH_PASSWORD", "spring.elasticsearch.password");

		
		// If KAFKA_ENABLED is explicitly false, disable Kafka auto-startup
		String kafkaEnabled = System.getenv("KAFKA_ENABLED");
		if ("false".equalsIgnoreCase(kafkaEnabled)) {
			System.setProperty("spring.kafka.listener.auto-startup", "false");
			System.setProperty("spring.kafka.admin.auto-create", "false");
		}
		
		SpringApplication.run(FoodDeliveryApplication.class, args);
	}

	private static void cleanAndSetProperty(String envVar, String systemProp) {
		String value = System.getenv(envVar);
		if (value != null) {
			// Remove literal "\n", "\r", and quotes
			String cleanValue = value.trim()
                .replace("\\n", "")
                .replace("\\r", "")
                .replace("\n", "")
                .replace("\r", "");
			
			if (cleanValue.startsWith("\"") && cleanValue.endsWith("\"")) {
				cleanValue = cleanValue.substring(1, cleanValue.length() - 1).trim();
			}
            // Double check for quotes if nested
            if (cleanValue.startsWith("\"") && cleanValue.endsWith("\"")) {
				cleanValue = cleanValue.substring(1, cleanValue.length() - 1).trim();
			}

			System.setProperty(systemProp, cleanValue);
			System.out.println("DEBUG: Cleaned " + envVar + " -> [" + cleanValue + "]");
		}
	}
}
