package com.delivery.foodDelivery.service;

import com.delivery.foodDelivery.entity.User;
import com.delivery.foodDelivery.entity.Wallet;
import com.delivery.foodDelivery.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {

    private final WalletRepository walletRepository;

    /**
     * Initialize a new wallet for a user with a welcome bonus.
     */
    @Transactional
    public void createWallet(User user) {
        Wallet wallet = Wallet.builder()
                .user(user)
                .balance(50.0) // Welcome Bonus!
                .goldMember(false)
                .build();
        walletRepository.save(wallet);
        log.info("Wallet initialized for user: {} with 50 coins", user.getEmail());
    }

    @Transactional(readOnly = true)
    public Wallet getWalletByUserId(Long userId) {
        return walletRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user: " + userId));
    }

    @Transactional
    public void addCoins(Long userId, Double amount) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.addCoins(amount);
        walletRepository.save(wallet);
        log.info("Credited {} coins to user: {}", amount, userId);
    }

    @Transactional
    public void spendCoins(Long userId, Double amount) {
        Wallet wallet = getWalletByUserId(userId);
        wallet.spendCoins(amount);
        walletRepository.save(wallet);
        log.info("Debited {} coins from user: {}", amount, userId);
    }
}
