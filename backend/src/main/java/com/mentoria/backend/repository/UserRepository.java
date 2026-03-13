package com.mentoria.backend.repository;

import com.mentoria.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID>{

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Modifying
    @Query("UPDATE User u SET u.lastLoginAt = :now WHERE u.id = :id")
    void updateLastLogin(@Param("now") LocalDateTime now, @Param("id") UUID id);

    @Modifying
    @Query("UPDATE User u Set u.planType = :planType WHERE u.id = :userId")
    void updatePlanType(@Param("planType") User.PlanType planType, @Param("userId") UUID userId);
}
