package com.delivery.foodDelivery.config;

import com.delivery.foodDelivery.entity.Restaurant;
import com.delivery.foodDelivery.entity.User;
import com.delivery.foodDelivery.enums.Role;
import com.delivery.foodDelivery.repository.RestaurantRepository;
import com.delivery.foodDelivery.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Seed Admin User
        if (userRepository.findByEmail("admin@tomato.com").isEmpty()) {
            User admin = User.builder()
                    .name("Priyanshu Admin")
                    .email("admin@tomato.com")
                    .password(passwordEncoder.encode("admin123"))
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .phone("9988776655")
                    .role(Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
        }

        // Seed Sample Restaurants if empty
        if (restaurantRepository.count() == 0) {
            List<Restaurant> sampleRestaurants = List.of(
                Restaurant.builder()
                    .name("Pizza Palace")
                    .address("Sector 62, Noida")
                    .city("Noida")
                    .cuisineType("Italian, Fast Food")
                    .phone("0120-123456")
                    .imageUrl("https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800&q=80")
                    .rating(4.5)
                    .avgDeliveryTime(30)
                    .minOrderAmount(200.0)
                    .open(true)
                    .active(true)
                    .build(),
                Restaurant.builder()
                    .name("Burger King")
                    .address("DLF Mall of India")
                    .city("Noida")
                    .cuisineType("Burgers, American")
                    .phone("0120-654321")
                    .imageUrl("https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=800&q=80")
                    .rating(4.2)
                    .avgDeliveryTime(25)
                    .minOrderAmount(150.0)
                    .open(true)
                    .active(true)
                    .build(),
                Restaurant.builder()
                    .name("The Biryani Life")
                    .address("Indirapuram, Ghaziabad")
                    .city("Ghaziabad")
                    .cuisineType("Biryani, North Indian")
                    .phone("0120-987654")
                    .imageUrl("https://images.unsplash.com/photo-1563379091339-03b21bc4a4f8?w=800&q=80")
                    .rating(4.8)
                    .avgDeliveryTime(40)
                    .minOrderAmount(250.0)
                    .open(true)
                    .active(true)
                    .build()
            );
            restaurantRepository.saveAll(sampleRestaurants);
        }
    }
}
