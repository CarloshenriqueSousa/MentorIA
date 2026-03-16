package com.mentoria.backend.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class DashboardResponse {

    private int totalMinutesStudied;
    private int currentStreakDays;
    private int maxStreakDays;
    private int totalXp;
    private int totalExercisesDone;
    private double exerciseAccuracyRate;
    private StudyPlanSummary activePlan;
    private List<DailyProgress> weeklyProgress;
    private List<Achievement> achievements;
    private String nextSessionRecommendation;

    @Data
    @Builder
    public static class DailyProgress {
        private LocalDate date;
        private int minutesStudied;
        private int xpEarned;
        private boolean goalReached;
    }

    @Data
    @Builder
    public static class StudyPlanSummary {
        private String id;
        private String title;
        private int completionPercentage;
        private LocalDate endDate;
        private int daysRemaining;
    }

    @Data
    @Builder
    public static class Achievement {
        private String id;
        private String name;
        private String description;
        private String icon;
        private boolean unlocked;
        private LocalDate unlockedAt;
    }
}