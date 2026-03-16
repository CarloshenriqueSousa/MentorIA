package com.mentoria.backend.repository;

import com.mentoria.backend.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByUser_Id(UUID userId);

    Optional<Subscription> findByStripeSubscriptionId(String stripeSubscriptionId);
}