package com.delivery.foodDelivery.repository.jpa;

import com.delivery.foodDelivery.entity.User;
import com.delivery.foodDelivery.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    List<User> findByRole(Role role);

    List<User> findByActiveTrue();

    Page<User> findByActiveTrue(Pageable pageable);

    long countByRole(Role role);
}

