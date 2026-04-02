package com.mentoria.backend.service;

import com.mentoria.backend.dto.request.UpdateProfileRequest;
import com.mentoria.backend.dto.request.UpdateSettingsRequest;
import com.mentoria.backend.dto.response.UserSettingsResponse;
import com.mentoria.backend.exception.ResourceNotFoundException;
import com.mentoria.backend.model.User;
import com.mentoria.backend.model.UserProfile;
import com.mentoria.backend.repository.UserProfileRepository;
import com.mentoria.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

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
}
