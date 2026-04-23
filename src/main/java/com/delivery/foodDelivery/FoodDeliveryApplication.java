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
		SpringApplication.run(FoodDeliveryApplication.class, args);
	}
}
