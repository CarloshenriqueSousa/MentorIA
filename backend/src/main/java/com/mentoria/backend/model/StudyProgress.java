package com.mentoria.backend.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "study_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "study_date"}))
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder

public class StudyProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "study_plan_id")
    private StudyPlan studyPlan;

    @Column(name = "study_date", nullable = false)
    private LocalDate studyDate;

    @Column(name = "minutes_studied", nullable = false)
    @Builder.Default
    private int minutesStudied = 0;

    @Type(JsonBinaryType.class)
    @Column(name = "topics_completed", columnDefinition = "jsonb")
    private List<String> topicsCompleted;

    @Column(name = "exercises_done")
    @Builder.Default
    private int exercisesDone = 0;

    @Column(name = "exercises_correct")
    @Builder.Default
    private int exercisesCorrect = 0;

    @Column(name = "streak_day")
    @Builder.Default
    private int streakDay = 0;

    @Column(name = "mood_rating")
    private Integer moodRating;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "xp_earned")
    @Builder.Default
    private int xpEarned = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}