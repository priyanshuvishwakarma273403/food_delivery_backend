package com.delivery.foodDelivery.config;

import com.delivery.foodDelivery.entity.Restaurant;
import com.delivery.foodDelivery.entity.MenuItem;
import com.delivery.foodDelivery.entity.User;
import com.delivery.foodDelivery.enums.Role;
import com.delivery.foodDelivery.repository.mongo.RestaurantRepository;
import com.delivery.foodDelivery.repository.mongo.MenuItemRepository;
import com.delivery.foodDelivery.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

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
            log.info("Admin user seeded.");
        }

        // Seed Massive Data for Restaurants and MenuItems
        if (restaurantRepository.count() < 100) {
            seedMassiveData();
        }
    }

    private String getRandomImage(String cuisine) {
        String[] burgerImages = {
            "https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=800",
            "https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=800",
            "https://images.unsplash.com/photo-1550547660-d9450f859349?w=800"
        };
        String[] pizzaImages = {
            "https://images.unsplash.com/photo-1513104890138-7c749659a591?w=800",
            "https://images.unsplash.com/photo-1574129682662-e109127d41a9?w=800",
            "https://images.unsplash.com/photo-1593560708920-61dd98c46a4e?w=800"
        };
        String[] saladImages = {
            "https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=800",
            "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=800"
        };
        String[] generalFood = {
            "https://images.unsplash.com/photo-1476224203421-9ac39bcb3327?w=800",
            "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?w=800",
            "https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=800"
        };

        String cuisineLower = cuisine.toLowerCase();
        if (cuisineLower.contains("burger")) return burgerImages[random.nextInt(burgerImages.length)];
        if (cuisineLower.contains("pizza")) return pizzaImages[random.nextInt(pizzaImages.length)];
        if (cuisineLower.contains("salad") || cuisineLower.contains("healthy")) return saladImages[random.nextInt(saladImages.length)];
        
        return generalFood[random.nextInt(generalFood.length)];
    }

    private void seedMassiveData() {
        log.info("Starting Massive Data Seeding: 100 Restaurants and 1000 Menu Items...");
        
        String[] restaurantNames = {
            "Pizza Palace", "Burger King", "The Biryani Life", "Taco Bell", "Sushi Sam", "Pasta Perfection",
            "Curry House", "Wok Hei", "Steak & Grill", "Vegan Vibes", "Dessert Heaven", "Biryani Blues",
            "Momo Magic", "Tandoori Nights", "Burger Baron", "Pizza Hut", "Dosa Plaza", "Chai Point",
            "The Salad Bar", "Smoothie Stop", "Noodle Station", "Kebab Korner", "BBQ Nation", "Punjab Grill"
        };
        
        String[] cities = {"Noida", "Ghaziabad", "Delhi", "Gurgaon", "Mumbai", "Bangalore", "Pune", "Hyderabad"};
        String[] cuisines = {"Indian", "Chinese", "Italian", "American", "Mexican", "Japanese", "Continental", "Thai"};
        String[] categories = {"VEG", "NON_VEG", "DESSERT", "BEVERAGES", "SIDES"};

        String[] restaurantImages = {
            "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1552566626-52f8b828add9?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1555396273-367ea4eb4db5?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1514933651103-005eec06c04b?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1414235077428-338989a2e8c0?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1504674900247-0877df9cc836?auto=format&fit=crop&w=800&q=80",
            "https://images.unsplash.com/photo-1502301103665-0b95cc738def?auto=format&fit=crop&w=800&q=80"
        };

        List<Restaurant> seededRestaurants = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            String cuisine = cuisines[random.nextInt(cuisines.length)];
            String name = restaurantNames[random.nextInt(restaurantNames.length)] + " " + (i + 1);
            Restaurant restaurant = Restaurant.builder()
                    .name(name)
                    .address("Street " + (i + 1) + ", " + cities[random.nextInt(cities.length)])
                    .city(cities[random.nextInt(cities.length)])
                    .cuisineType(cuisine)
                    .phone("9" + (100000000 + i))
                    .imageUrl(getRandomImage(cuisine))
                    .rating(3.0 + (random.nextDouble() * 2.0))
                    .avgDeliveryTime(20 + random.nextInt(40))
                    .minOrderAmount(100.0 + random.nextInt(200))
                    .open(true)
                    .build();
            
            seededRestaurants.add(restaurantRepository.save(restaurant));
        }

        log.info("100 Restaurants seeded. Now seeding 1000 Menu Items...");

        for (Restaurant r : seededRestaurants) {
            List<MenuItem> items = new ArrayList<>();
            for (int j = 0; j < 10; j++) {
                boolean isVeg = random.nextBoolean();
                String category = categories[random.nextInt(categories.length)];
                
                MenuItem item = MenuItem.builder()
                        .name(r.getName().split(" ")[0] + " Special " + (j + 1))
                        .description("Delicious " + category.toLowerCase() + " item from " + r.getName())
                        .price(99.0 + random.nextInt(500))
                        .imageUrl(getRandomImage(r.getCuisineType()))
                        .category(category)
                        .restaurantId(r.getId())
                        .vegetarian(isVeg)
                        .available(true)
                        .build();
                items.add(item);
            }
            menuItemRepository.saveAll(items);
        }

        log.info("Massive Seeding Completed: 100 Restaurants and 1000 Menu Items created!");
    }
}
