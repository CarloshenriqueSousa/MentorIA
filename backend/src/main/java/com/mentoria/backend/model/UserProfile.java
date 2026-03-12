package com.mentoria.backend.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;
import java.util.Map;
import java.util.List;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserProfile {

    @Id
    @GeneratedValue(strategy =GenerationType.UUID)
    private UUID Id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User User;

    @Column(name = "target_exam")
    private String TargetExam;

    @Enumerated(EnumType.STRING)
    @Column(name = "knowledge_level")
    @Builder.Default
    private KnowledLevel KnowLegLevel = KnowledLevel.BEGINNER;

    @Column(name = "study_hours_per_day")
    @Builder.Default
    private Integer StudyHoursPerDay = 1;

    @Type(JsonBinaryType.class)
    @Column(name = "available_days", columnDefinition = "jsonb")
    private List<String> Availabledays;

    @Type(JsonBinaryType.class)
    @Column(name = "objectives", columnDefinition = "jsonb")
    private List<String> objectives;

    @Type(JsonBinaryType.class)
    @Column(name = "difficulties", columnDefinition = "jsonb")
    private List<String> difficulties;

    @Type(JsonBinaryType.class)
    @Column(name = "strengths", columnDefinition = "jsonb")
    private List<String> strengths;

    @Type(JsonBinaryType.class)
    @Column(name = "subjects_priority", columnDefinition = "jsonb")
    private List<Map<String, Object>> SubjectsPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_style")
    @Builder.Default
    private LearningStyle learningStyle = LearningStyle.MIXED;

    @Column(name = "completed_onboarding", nullable = false)
    @Builder.Default
    private String CompletedOnBoarding;

    @Type(JsonBinaryType.class)
    @Column(name = "extra_info", columnDefinition = "jsonb")
    private Map<String, Object> ExtraInfo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime CreatedAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime UpdatedAt;

    public enum LearningStyle {
        VISUAL, READING, PRATICAL, MIXED
    }

    public enum KnowledLevel {
        BEGINNER, INTERMEDIARY, ADVANCED
    }

}
