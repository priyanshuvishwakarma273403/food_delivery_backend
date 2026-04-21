package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.entity.SocialPost;
import com.delivery.foodDelivery.entity.User;
import com.delivery.foodDelivery.repository.jpa.SocialPostRepository;
import com.delivery.foodDelivery.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SocialService {

    private final SocialPostRepository socialPostRepository;
    private final UserRepository userRepository;
    private final WalletService walletService;

    public List<SocialPost> getFeed() {
        return socialPostRepository.findAllByOrderByCreatedDateDesc();
    }

    @Transactional
    public SocialPost createPost(Long userId, String imageUrl, String caption) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        SocialPost post = SocialPost.builder()
                .user(user)
                .imageUrl(imageUrl)
                .caption(caption)
                .build();

        post = socialPostRepository.save(post);

        // Reward user with 50 coins for the first post of the day (simplified)
        walletService.addCoins(user, 50.0);
        
        return post;
    }
}
