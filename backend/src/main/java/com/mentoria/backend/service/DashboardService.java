package com.mentoria.backend.service;

import com.mentoria.backend.dto.request.StudyProgressRequest;
import com.mentoria.backend.dto.response.DashboardResponse;
import com.mentoria.backend.exception.ResourceNotFoundException;
import com.mentoria.backend.model.*;
import com.mentoria.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {

    private final StudyProgressRepository progressRepository;
    private final StudyPlanRepository planRepository;
    private final UserRepository userRepository;

    private static final int XP_PER_MINUTE = 1;
    private static final int XP_PER_EXERCISE = 5;
    private static final int XP_STREAK_BONUS = 20;
    private static final int XP_DAILY_GOAL = 50;

    @Transactional
    public StudyProgress logProgress(UUID userId, StudyProgressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        LocalDate date = request.getStudyDate() != null ? request.getStudyDate() : LocalDate.now();

        StudyProgress progress = progressRepository.findByUser_IdAndStudyDate(userId, date)
                .orElse(StudyProgress.builder().user(user).studyDate(date).build());

        progress.setMinutesStudied(progress.getMinutesStudied() +
                (request.getMinutesStudied() != null ? request.getMinutesStudied() : 0));

        if (request.getTopicsCompleted() != null) progress.setTopicsCompleted(request.getTopicsCompleted());
        if (request.getExercisesDone() != null)
            progress.setExercisesDone(progress.getExercisesDone() + request.getExercisesDone());
        if (request.getExercisesCorrect() != null)
            progress.setExercisesCorrect(progress.getExercisesCorrect() + request.getExercisesCorrect());
        if (request.getMoodRating() != null) progress.setMoodRating(request.getMoodRating());
        if (request.getNotes() != null) progress.setNotes(request.getNotes());

        if (request.getStudyPlanId() != null) {
            planRepository.findByIdAndUser_Id(request.getStudyPlanId(), userId)
                    .ifPresent(progress::setStudyPlan);
        }

        int streak = calculateStreak(userId, date);
        progress.setStreakDay(streak);
        progress.setXpEarned(calculateXP(progress, streak));

        return progressRepository.save(progress);
    }

    public DashboardResponse getDashboard(UUID userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);

        int totalMinutes = progressRepository.totalMinutesByUserId(userId).orElse(0);
        int maxStreak = progressRepository.maxStreakByUserId(userId).orElse(0);
        int totalXp = progressRepository.totalXpByUserId(userId).orElse(0);
        int currentStreak = calculateStreak(userId, today);

        List<StudyProgress> weekProgress = progressRepository
                .findByUser_IdAndStudyDateBetweenOrderByStudyDateAsc(userId, weekAgo, today);

        int totalExercises = weekProgress.stream().mapToInt(StudyProgress::getExercisesDone).sum();
        int totalCorrect = weekProgress.stream().mapToInt(StudyProgress::getExercisesCorrect).sum();
        double accuracy = totalExercises > 0 ? (double) totalCorrect / totalExercises * 100 : 0;

        DashboardResponse.StudyPlanSummary planSummary = planRepository
                .findByUser_IdAndIsActiveTrue(userId)
                .map(plan -> DashboardResponse.StudyPlanSummary.builder()
                        .id(plan.getId().toString())
                        .title(plan.getTitle())
                        .completionPercentage(plan.getCompletionPercentage())
                        .endDate(plan.getEndDate())
                        .daysRemaining(plan.getEndDate() != null
                                ? (int) ChronoUnit.DAYS.between(today, plan.getEndDate()) : 0)
                        .build())
                .orElse(null);

        return DashboardResponse.builder()
                .totalMinutesStudied(totalMinutes)
                .currentStreakDays(currentStreak)
                .maxStreakDays(maxStreak)
                .totalXp(totalXp)
                .totalExercisesDone(totalExercises)
                .exerciseAccuracyRate(Math.round(accuracy * 10.0) / 10.0)
                .activePlan(planSummary)
                .weeklyProgress(buildDailyProgress(weekProgress, weekAgo, today))
                .achievements(buildAchievements(currentStreak, maxStreak, totalXp, totalMinutes, totalExercises))
                .nextSessionRecommendation(buildRecommendation(weekProgress))
                .build();
    }

    private int calculateStreak(UUID userId, LocalDate date) {
        return progressRepository.findFirstByUser_IdOrderByStudyDateDesc(userId)
                .map(last -> {
                    long diff = ChronoUnit.DAYS.between(last.getStudyDate(), date);
                    if (diff == 0) return last.getStreakDay();
                    if (diff == 1) return last.getStreakDay() + 1;
                    return 1;
                }).orElse(1);
    }

    private int calculateXP(StudyProgress p, int streak) {
        int xp = p.getMinutesStudied() * XP_PER_MINUTE;
        xp += p.getExercisesDone() * XP_PER_EXERCISE;
        if (streak > 1) xp += XP_STREAK_BONUS;
        if (p.getMinutesStudied() >= 60) xp += XP_DAILY_GOAL;
        return xp;
    }

    private List<DashboardResponse.DailyProgress> buildDailyProgress(
            List<StudyProgress> list, LocalDate start, LocalDate end) {
        Map<LocalDate, StudyProgress> map = list.stream()
                .collect(Collectors.toMap(StudyProgress::getStudyDate, p -> p));
        List<DashboardResponse.DailyProgress> result = new ArrayList<>();
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            StudyProgress p = map.get(d);
            result.add(DashboardResponse.DailyProgress.builder()
                    .date(d)
                    .minutesStudied(p != null ? p.getMinutesStudied() : 0)
                    .xpEarned(p != null ? p.getXpEarned() : 0)
                    .goalReached(p != null && p.getMinutesStudied() >= 60)
                    .build());
        }
        return result;
    }

    private List<DashboardResponse.Achievement> buildAchievements(
            int streak, int maxStreak, int xp, int minutes, int exercises) {
        return List.of(
                achievement("first_day", "Primeiro Passo", "🎯", "Completou o primeiro dia", minutes > 0),
                achievement("streak_7", "Semana Consistente", "🔥", "7 dias seguidos", maxStreak >= 7),
                achievement("streak_30", "Mês de Dedicação", "💪", "30 dias seguidos", maxStreak >= 30),
                achievement("exercises_100", "Praticante", "📝", "100 exercícios resolvidos", exercises >= 100),
                achievement("xp_1000", "Estudante Dedicado", "⭐", "1.000 XP acumulados", xp >= 1000),
                achievement("hours_50", "50 Horas de Estudo", "🏆", "50 horas totais", minutes >= 3000)
        );
    }

    private DashboardResponse.Achievement achievement(
            String id, String name, String icon, String desc, boolean unlocked) {
        return DashboardResponse.Achievement.builder()
                .id(id).name(name).icon(icon).description(desc).unlocked(unlocked).build();
    }

    private String buildRecommendation(List<StudyProgress> weekProgress) {
        if (weekProgress.isEmpty()) return "Comece sua primeira sessão hoje! Que tal 30 minutos?";
        StudyProgress last = weekProgress.get(weekProgress.size() - 1);
        if (last.getStudyDate().isBefore(LocalDate.now().minusDays(1)))
            return "Você não estudou ontem! Estude pelo menos 30 minutos hoje.";
        if (last.getMinutesStudied() < 30) return "Ontem foi pouco! Tente superar com 1 hora hoje.";
        return "Ótimo ritmo! Continue com " + last.getMinutesStudied() + " minutos ou mais hoje.";
    }
}