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
		// Robust fix for environment variables with trailing newlines or literal "\n" strings
		String jdbcUrl = System.getenv("SPRING_DATASOURCE_URL");
		if (jdbcUrl != null) {
			// Clean the URL: trim whitespace, remove trailing literal "\n", and remove surrounding quotes
			String cleanUrl = jdbcUrl.trim();
			if (cleanUrl.endsWith("\\n")) {
				cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 2).trim();
			}
			if (cleanUrl.startsWith("\"") && cleanUrl.endsWith("\"")) {
				cleanUrl = cleanUrl.substring(1, cleanUrl.length() - 1).trim();
			}
			System.setProperty("spring.datasource.url", cleanUrl);
			System.out.println("DEBUG: Cleaned JDBC URL: " + cleanUrl);
		}
		
		SpringApplication.run(FoodDeliveryApplication.class, args);
	}
}
