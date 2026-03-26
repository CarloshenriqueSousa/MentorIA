package com.mentoria.backend.service;

import com.mentoria.backend.dto.request.ChangePasswordRequest;
import com.mentoria.backend.dto.request.UpdateProfileRequest;
import com.mentoria.backend.dto.request.UpdateSettingsRequest;
import com.mentoria.backend.exception.BusinessException;
import com.mentoria.backend.model.User;
import com.mentoria.backend.model.UserProfile;
import com.mentoria.backend.repository.UserProfileRepository;
import com.mentoria.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));
        user.setName(request.getName());
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        if (user.getPasswordHash() == null) {
            throw new BusinessException("Sua conta utiliza autenticação externa. Altere a senha pelo provedor.");
        }

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BusinessException("Senha atual incorreta");
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("As senhas não coincidem");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void updateSettings(UUID userId, UpdateSettingsRequest request) {
        UserProfile profile = userProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new BusinessException("Perfil não encontrado"));

        if (request.getDailyGoalMinutes() != null) {
            profile.setDailyGoalMinutes(request.getDailyGoalMinutes());
        }
        if (request.getMentorStyle() != null) {
            profile.setMentorStyle(request.getMentorStyle());
        }

        // Settings Map (language, notifications)
        if (profile.getExtraInfo() == null) {
            profile.setExtraInfo(new java.util.HashMap<>());
        }

        if (request.getLanguage() != null) {
            profile.getExtraInfo().put("language", request.getLanguage());
        }
        if (request.getNotifications() != null) {
            profile.getExtraInfo().put("notifications", request.getNotifications());
        }

        userProfileRepository.save(profile);
    }

    public UpdateSettingsRequest getSettings(UUID userId) {
        UserProfile profile = userProfileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new BusinessException("Perfil não encontrado"));

        java.util.Map<String, Object> extraInfo = profile.getExtraInfo() != null ? profile.getExtraInfo()
                : new java.util.HashMap<>();

        return UpdateSettingsRequest.builder()
                .dailyGoalMinutes(profile.getDailyGoalMinutes())
                .mentorStyle(profile.getMentorStyle())
                .language((String) extraInfo.get("language"))
                .notifications((Boolean) extraInfo.get("notifications"))
                .build();
    }
}
