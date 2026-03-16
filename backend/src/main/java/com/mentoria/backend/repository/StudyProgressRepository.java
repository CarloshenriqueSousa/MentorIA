package com.mentoria.backend.repository;

import com.mentoria.backend.model.StudyProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StudyProgressRepository extends JpaRepository<StudyProgress, UUID> {

    Optional<StudyProgress> findByUser_IdAndStudyDate(UUID userId, LocalDate date);

    List<StudyProgress> findByUser_IdAndStudyDateBetweenOrderByStudyDateAsc(
            UUID userId, LocalDate startDate, LocalDate endDate);

    Optional<StudyProgress> findFirstByUser_IdOrderByStudyDateDesc(UUID userId);

    @Query("SELECT SUM(sp.minutesStudied) FROM StudyProgress sp WHERE sp.user.id = :userId")
    Optional<Integer> totalMinutesByUserId(UUID userId);

    @Query("SELECT MAX(sp.streakDay) FROM StudyProgress sp WHERE sp.user.id = :userId")
    Optional<Integer> maxStreakByUserId(UUID userId);


    @Query("SELECT SUM(sp.xpEarned) FROM StudyProgress sp WHERE sp.user.id = :userId")
    Optional<Integer> totalXpByUserId(UUID userId);
}