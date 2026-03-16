package com.mentoria.backend.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mentoria.backend.exception.BusinessException;
import com.mentoria.backend.exception.ResourceNotFoundException;
import com.mentoria.backend.model.StudyPlan;
import com.mentoria.backend.model.User;
import com.mentoria.backend.model.UserProfile;
import com.mentoria.backend.repository.StudyPlanRepository;
import com.mentoria.backend.repository.UserProfileRepository;
import com.mentoria.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudyPlanService {

    private final StudyPlanRepository planRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final AiService aiService;

    @Value("${freemium.monthly-plan-limit}")
    private int monthlyPlanLimit;

    @Transactional
    public StudyPlan generatePlan(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        UserProfile profile = profileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new BusinessException("Complete o onboarding antes de gerar um plano"));

        if (!profile.isCompletedOnboarding()) {
            throw new BusinessException("Complete o onboarding antes de gerar um plano");
        }

        if (user.getPlanType() == User.PlanType.FREE) {
            long total = planRepository.countByUser_Id(userId);
            if (total >= monthlyPlanLimit) {
                throw new BusinessException(
                        "Limite de " + monthlyPlanLimit + " planos atingido. Faça upgrade!"
                );
            }
        }

        planRepository.findByUser_IdAndIsActiveTrue(userId).ifPresent(current -> {
            current.setActive(false);
            current.setStatus(StudyPlan.PlanStatus.ARCHIVED);
            planRepository.save(current);
        });

        Map<String, Object> aiPlan = aiService.generateStudyPlan(profile, user);

        int durationWeeks = (profile.getStudyHoursPerDay() != null
                && profile.getStudyHoursPerDay() >= 4) ? 8 : 12;

        StudyPlan plan = StudyPlan.builder()
                .user(user)
                .title((String) aiPlan.getOrDefault("title", "Plano - " + profile.getTargetExam()))
                .description((String) aiPlan.get("description"))
                .planContent((Map<String, Object>) aiPlan.get("plan_content"))
                .subjectsCovered((List<String>) aiPlan.getOrDefault("subjects_covered", List.of()))
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusWeeks(durationWeeks))
                .isActive(true)
                .status(StudyPlan.PlanStatus.ACTIVE)
                .build();

        StudyPlan saved = planRepository.save(plan);
        log.info("Plano gerado para usuário: {}", userId);
        return saved;
    }

    public List<StudyPlan> getUserPlans(UUID userId) {
        return planRepository.findByUser_IdOrderByCreatedAtDesc(userId);
    }

    public StudyPlan getActivePlan(UUID userId) {
        return planRepository.findByUser_IdAndIsActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Nenhum plano ativo encontrado"));
    }

    public StudyPlan getPlan(UUID userId, UUID planId) {
        return planRepository.findByIdAndUser_Id(planId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Plano não encontrado"));
    }

    @Transactional
    public StudyPlan updateStatus(UUID userId, UUID planId, StudyPlan.PlanStatus status) {
        StudyPlan plan = getPlan(userId, planId);
        plan.setStatus(status);
        if (status == StudyPlan.PlanStatus.PAUSED || status == StudyPlan.PlanStatus.ARCHIVED) {
            plan.setActive(false);
        }
        return planRepository.save(plan);
    }
}