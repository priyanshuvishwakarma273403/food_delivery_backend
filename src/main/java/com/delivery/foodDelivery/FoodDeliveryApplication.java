package com.delivery.foodDelivery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableJpaAuditing
@EnableCaching
@EnableAsync
@EnableJpaRepositories(basePackages = "com.delivery.foodDelivery.repository.jpa")
@EnableMongoRepositories(basePackages = "com.delivery.foodDelivery.repository.mongo")
public class FoodDeliveryApplication {


	public static void main(String[] args) {
		SpringApplication.run(FoodDeliveryApplication.class, args);
	}
}
