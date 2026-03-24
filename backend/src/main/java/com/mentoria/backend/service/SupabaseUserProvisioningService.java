package com.mentoria.backend.service;

import com.mentoria.backend.exception.ApiUnauthorizedException;
import com.mentoria.backend.model.User;
import com.mentoria.backend.model.UserProfile;
import com.mentoria.backend.repository.UserProfileRepository;
import com.mentoria.backend.repository.UserRepository;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupabaseUserProvisioningService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User ensureLocalUserFromSupabaseClaims(Claims claims) {
        UUID supabaseId = UUID.fromString(claims.getSubject());
        String email = claims.get("email", String.class);
        if (email == null || email.isBlank()) {
            throw new ApiUnauthorizedException("Token do Supabase sem email");
        }
        email = email.toLowerCase().trim();
        String name = extractDisplayName(claims, email);

        Optional<User> bySupabase = userRepository.findBySupabaseUserId(supabaseId);
        if (bySupabase.isPresent()) {
            User u = bySupabase.get();
            userRepository.updateLastLogin(u.getId(), LocalDateTime.now());
            return u;
        }

        Optional<User> byEmail = userRepository.findByEmail(email);
        if (byEmail.isPresent()) {
            User u = byEmail.get();
            u.setSupabaseUserId(supabaseId);
            if (u.getName() == null || u.getName().isBlank()) {
                u.setName(name);
            }
            userRepository.save(u);
            userRepository.updateLastLogin(u.getId(), LocalDateTime.now());
            ensureProfile(u);
            return u;
        }

        User user = User.builder()
                .email(email)
                .name(name)
                .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                .supabaseUserId(supabaseId)
                .planType(User.PlanType.FREE)
                .build();
        user = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .completedOnboarding(false)
                .build();
        profileRepository.save(profile);

        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());
        log.info("Usuário criado a partir do Supabase Auth: {}", email);
        return user;
    }

    private void ensureProfile(User user) {
        if (profileRepository.findByUser_Id(user.getId()).isEmpty()) {
            profileRepository.save(UserProfile.builder()
                    .user(user)
                    .completedOnboarding(false)
                    .build());
        }
    }

    @SuppressWarnings("unchecked")
    private static String extractDisplayName(Claims claims, String email) {
        Map<String, Object> meta = claims.get("user_metadata", Map.class);
        if (meta != null) {
            Object full = meta.get("full_name");
            if (full instanceof String s && !s.isBlank()) {
                return s.trim();
            }
            Object n = meta.get("name");
            if (n instanceof String s && !s.isBlank()) {
                return s.trim();
            }
        }
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}
