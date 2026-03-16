package com.mentoria.backend.repository;

import com.mentoria.backend.model.MessageUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageUsageRepository extends JpaRepository<MessageUsage, UUID> {

    Optional<MessageUsage> findByUser_IdAndUsageDate(UUID userId, LocalDate date);
}