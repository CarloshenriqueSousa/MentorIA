package com.mentoria.backend.repository;

import com.mentoria.backend.model.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;
import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, UUID>{

    List<ChatSession> findByUserIdAndIsActiveTrueOrderByUpdatedAtDesc(UUID userId);

    Optional<ChatSession> findUserByIdAndId(UUID id, UUID userId);

    long countByUserId(UUID userId);
}
