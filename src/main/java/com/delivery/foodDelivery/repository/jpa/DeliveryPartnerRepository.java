package com.delivery.foodDelivery.repository.jpa;

import com.delivery.foodDelivery.entity.DeliveryPartner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryPartnerRepository extends JpaRepository<DeliveryPartner, Long> {
    Optional<DeliveryPartner> findByUserId(Long userId);
    List<DeliveryPartner> findByCurrentCityAndAvailableTrue(String city);
}
