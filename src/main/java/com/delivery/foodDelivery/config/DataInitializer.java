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

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;
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
            log.info("Admin user seeded.");
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
                    .build()
            );
            restaurantRepository.saveAll(sampleRestaurants);
            log.info("Restaurants seeded.");
        }

        // Seed Menu Items if empty
        if (menuItemRepository.count() == 0) {
            List<Restaurant> restaurants = restaurantRepository.findAll();
            restaurants.forEach(r -> {
                if (r.getName().equals("Pizza Palace")) {
                    seedPizzaPalaceMenu(r.getId());
                } else if (r.getName().equals("Burger King")) {
                    seedBurgerKingMenu(r.getId());
                } else if (r.getName().equals("The Biryani Life")) {
                    seedBiryaniLifeMenu(r.getId());
                }
            });
            log.info("Menu items seeded.");
        }
    }

    private void seedPizzaPalaceMenu(String rId) {
        menuItemRepository.saveAll(List.of(
            MenuItem.builder().name("Margherita Pizza").description("Classic cheese and tomato").price(299.0).imageUrl("https://images.unsplash.com/photo-1574071318508-1cdbad80ad50?w=500").category("VEG").restaurantId(rId).vegetarian(true).build(),
            MenuItem.builder().name("Peppy Paneer").description("Spiced paneer, capsicum, onion").price(399.0).imageUrl("https://images.unsplash.com/photo-1513104890138-7c749659a591?w=500").category("VEG").restaurantId(rId).vegetarian(true).build(),
            MenuItem.builder().name("Chicken Dominator").description("Loaded with double chicken").price(499.0).imageUrl("https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=500").category("NON_VEG").restaurantId(rId).vegetarian(false).build()
        ));
    }

    private void seedBurgerKingMenu(String rId) {
        menuItemRepository.saveAll(List.of(
            MenuItem.builder().name("Whopper").description("Flame-grilled beef patty").price(199.0).imageUrl("https://images.unsplash.com/photo-1571091718767-18b5b1457add?w=500").category("NON_VEG").restaurantId(rId).vegetarian(false).build(),
            MenuItem.builder().name("Veggie Burger").description("Delicious plant-based patty").price(149.0).imageUrl("https://images.unsplash.com/photo-1550547660-d9450f859349?w=500").category("VEG").restaurantId(rId).vegetarian(true).build(),
            MenuItem.builder().name("French Fries").description("Classic salted fries").price(99.0).imageUrl("https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=500").category("SIDES").restaurantId(rId).vegetarian(true).build()
        ));
    }

    private void seedBiryaniLifeMenu(String rId) {
        menuItemRepository.saveAll(List.of(
            MenuItem.builder().name("Hyderabadi Chicken Biryani").description("Authentic slow-cooked biryani").price(349.0).imageUrl("https://images.unsplash.com/photo-1563379091339-03b21bc4a4f8?w=500").category("NON_VEG").restaurantId(rId).vegetarian(false).build(),
            MenuItem.builder().name("Lucknowi Mutton Biryani").description("Fragrant and tender mutton").price(449.0).imageUrl("https://images.unsplash.com/photo-1633945274405-b6c8069047b0?w=500").category("NON_VEG").restaurantId(rId).vegetarian(false).build(),
            MenuItem.builder().name("Veg Dum Biryani").description("Spiced veggies with basmati rice").price(249.0).imageUrl("https://images.unsplash.com/photo-1589302168068-964664d93dc0?w=500").category("VEG").restaurantId(rId).vegetarian(true).build()
        ));
    }
}
