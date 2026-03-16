package com.mentoria.backend.repository;

import com.mentoria.backend.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID> {

    List<ChatSession> findByUser_IdAndIsActiveTrueOrderByUpdatedAtDesc(UUID userId);

    Optional<ChatSession> findByIdAndUser_Id(UUID id, UUID userId);

    long countByUser_Id(UUID userId);
}