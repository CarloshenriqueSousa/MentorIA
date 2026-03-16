package com.mentoria.backend.repository;

import com.mentoria.backend.model.StudyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudyPlanRepository extends JpaRepository<StudyPlan, UUID> {

    List<StudyPlan> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    Optional<StudyPlan> findByUser_IdAndIsActiveTrue(UUID userId);

    Optional<StudyPlan> findByIdAndUser_Id(UUID id, UUID userId);

    long countByUser_Id(UUID userId);
}