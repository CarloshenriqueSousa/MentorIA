package com.mentoria.backend.repository;

import com.mentoria.backend.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, UUID>{

    Optional<UserProfile> findByUser(UUID userId);

    boolean existByEmail(UUID userId);
}
