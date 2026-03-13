package com.mentoria.backend.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
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
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "target_exam")
    private String targetExam;

    @Enumerated(EnumType.STRING)
    @Column(name = "knowledge_level")
    @Builder.Default
    private KnowledgeLevel knowLegLevel = KnowledgeLevel.BEGINNER;

    @Column(name = "study_hours_per_day")
    @Builder.Default
    private Integer studyHoursPerDay = 1;

    @Type(JsonBinaryType.class)
    @Column(name = "available_days", columnDefinition = "jsonb")
    private List<String> available;

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
    private List<Map<String, Object>> subjectsPriority;

    @Enumerated(EnumType.STRING)
    @Column(name = "learning_style")
    @Builder.Default
    private LearningStyle learningStyle = LearningStyle.MIXED;

    @Column(name = "completed_onboarding", nullable = false)
    @Builder.Default
    private String completedOnBoarding;

    @Type(JsonBinaryType.class)
    @Column(name = "extra_info", columnDefinition = "jsonb")
    private Map<String, Object> extraInfo;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum LearningStyle {
        VISUAL, READING, PRACTICAL, MIXED
    }

    public enum KnowledgeLevel {
        BEGINNER, INTERMEDIARY, ADVANCED
    }

}
