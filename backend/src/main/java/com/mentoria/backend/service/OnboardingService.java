package com.mentoria.backend.service;

import com.mentoria.backend.dto.request.OnboardingRequest;
import com.mentoria.backend.exception.ResourceNotFoundException;
import com.mentoria.backend.model.User;
import com.mentoria.backend.model.UserProfile;
import com.mentoria.backend.repository.UserProfileRepository;
import com.mentoria.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {

    private final UserProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional
    public UserProfile saveOnboarding(UUID userId, OnboardingRequest request) {
        UserProfile profile = profileRepository.findByUser_Id(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
                    return UserProfile.builder().user(user).build();
                });

        profile.setTargetExam(request.getTargetExam());
        profile.setKnowledgeLevel(request.getKnowledgeLevel());
        profile.setStudyHoursPerDay(request.getStudyHoursPerDay());
        profile.setAvailableDays(request.getAvailableDays());
        profile.setObjectives(request.getObjectives());
        profile.setDifficulties(request.getDifficulties());
        profile.setStrengths(request.getStrengths());
        profile.setSubjectsPriority(request.getSubjectsPriority());
        profile.setExtraInfo(request.getExtraInfo());

        if (request.getLearningStyle() != null) {
            profile.setLearningStyle(request.getLearningStyle());
        }

        profile.setCompletedOnboarding(true);

        UserProfile saved = profileRepository.save(profile);
        log.info("Onboarding concluído para usuário: {}", userId);
        return saved;
    }

    public UserProfile getProfile(UUID userId) {
        return profileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado"));
    }

    @Transactional
    public UserProfile updateProfile(UUID userId, OnboardingRequest request) {
        return saveOnboarding(userId, request);
    }
}