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
		
		SpringApplication.run(FoodDeliveryApplication.class, args);
	}

	private static void cleanAndSetProperty(String envVar, String systemProp) {
		String value = System.getenv(envVar);
		if (value != null) {
			String cleanValue = value.trim();
			// Remove literal "\n"
			if (cleanValue.endsWith("\\n")) {
				cleanValue = cleanValue.substring(0, cleanValue.length() - 2).trim();
			}
			// Remove surrounding quotes
			if (cleanValue.startsWith("\"") && cleanValue.endsWith("\"")) {
				cleanValue = cleanValue.substring(1, cleanValue.length() - 1).trim();
			}
			System.setProperty(systemProp, cleanValue);
			System.out.println("DEBUG: Cleaned " + envVar + " -> " + cleanValue);
		}
	}
}
