package com.delivery.foodDelivery.repository.jpa;

import com.delivery.foodDelivery.entity.Delivery;
import com.delivery.foodDelivery.enums.DeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
    Optional<Delivery> findByOrderId(Long orderId);
    List<Delivery> findByDeliveryPartnerId(Long deliveryPartnerId);
    List<Delivery> findByStatus(DeliveryStatus status);
    List<Delivery> findByDeliveryPartnerIdAndStatus(Long deliveryPartnerId, DeliveryStatus status);
}
