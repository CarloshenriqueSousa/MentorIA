package com.mentoria.backend.service;

<<<<<<< Updated upstream
import com.mentoria.backend.dto.request.ChangePasswordRequest;
import com.mentoria.backend.dto.request.UpdateProfileRequest;
import com.mentoria.backend.dto.request.UpdateSettingsRequest;
import com.mentoria.backend.exception.BusinessException;
=======
import com.mentoria.backend.dto.request.UpdateProfileRequest;
import com.mentoria.backend.dto.request.UpdateSettingsRequest;
import com.mentoria.backend.dto.response.UserSettingsResponse;
import com.mentoria.backend.exception.ResourceNotFoundException;
>>>>>>> Stashed changes
import com.mentoria.backend.model.User;
import com.mentoria.backend.model.UserProfile;
import com.mentoria.backend.repository.UserProfileRepository;
import com.mentoria.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
<<<<<<< Updated upstream
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

=======
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
>>>>>>> Stashed changes
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
<<<<<<< Updated upstream
=======
@Slf4j
>>>>>>> Stashed changes
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
<<<<<<< Updated upstream
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
                .notifications((Map<String, Object>) extraInfo.get("notifications"))
                .build();
    }
=======

    @Transactional
    public User updateProfile(UUID userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        user.setName(request.getName().trim());
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public UserSettingsResponse getSettings(UUID userId) {
        UserProfile profile = userProfileRepository.findByUser_Id(userId).orElse(null);

        if (profile == null || profile.getExtraInfo() == null) {
            return UserSettingsResponse.builder().build();
        }

        Map<String, Object> extra = profile.getExtraInfo();

        return UserSettingsResponse.builder()
                .language(getStringOrDefault(extra, "language", "pt-BR"))
                .dailyGoalMinutes(getIntOrDefault(extra, "dailyGoalMinutes", 120))
                .mentorStyle(getStringOrDefault(extra, "mentorStyle", "balanced"))
                .dailyReminder(getBoolOrDefault(extra, "dailyReminder", true))
                .streakAlert(getBoolOrDefault(extra, "streakAlert", true))
                .planUpdate(getBoolOrDefault(extra, "planUpdate", false))
                .build();
    }

    @Transactional
    public UserSettingsResponse updateSettings(UUID userId, UpdateSettingsRequest request) {
        UserProfile profile = userProfileRepository.findByUser_Id(userId).orElse(null);

        if (profile == null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
            profile = UserProfile.builder()
                    .user(user)
                    .build();
        }

        Map<String, Object> extra = profile.getExtraInfo();
        if (extra == null) {
            extra = new HashMap<>();
        }

        if (request.getLanguage() != null) extra.put("language", request.getLanguage());
        if (request.getDailyGoalMinutes() != null) extra.put("dailyGoalMinutes", request.getDailyGoalMinutes());
        if (request.getMentorStyle() != null) extra.put("mentorStyle", request.getMentorStyle());
        if (request.getDailyReminder() != null) extra.put("dailyReminder", request.getDailyReminder());
        if (request.getStreakAlert() != null) extra.put("streakAlert", request.getStreakAlert());
        if (request.getPlanUpdate() != null) extra.put("planUpdate", request.getPlanUpdate());

        profile.setExtraInfo(extra);
        userProfileRepository.save(profile);

        return getSettings(userId);
    }

    @Transactional
    public void deleteAccount(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        // Soft-delete: desativa a conta
        user.setActive(false);
        user.setEmail("deleted_" + userId + "@removed.local");
        user.setName("Conta removida");
        userRepository.save(user);

        log.info("Account soft-deleted: userId={}", userId);
    }

    // ---- helpers ----

    private String getStringOrDefault(Map<String, Object> map, String key, String def) {
        Object val = map.get(key);
        return val instanceof String s ? s : def;
    }

    private Integer getIntOrDefault(Map<String, Object> map, String key, Integer def) {
        Object val = map.get(key);
        if (val instanceof Number n) return n.intValue();
        return def;
    }

    private Boolean getBoolOrDefault(Map<String, Object> map, String key, Boolean def) {
        Object val = map.get(key);
        if (val instanceof Boolean b) return b;
        return def;
    }
>>>>>>> Stashed changes
}
