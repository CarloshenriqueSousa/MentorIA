package com.mentoria.backend.bootstrap;

import com.mentoria.backend.config.SeedTestUserProperties;
import com.mentoria.backend.model.User;
import com.mentoria.backend.model.UserProfile;
import com.mentoria.backend.model.UserRole;
import com.mentoria.backend.repository.UserProfileRepository;
import com.mentoria.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Cria usuários locais de teste (admin + usuário comum) para login em {@code POST /auth/login}.
 * Desative com {@code app.seed.test-user.enabled=false}.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "app.seed.test-user.enabled", havingValue = "true")
public class SeedTestUserRunner implements ApplicationRunner {

    private final SeedTestUserProperties props;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedAdminIfMissing();
        seedUserIfMissing();
    }

    private void seedAdminIfMissing() {
        String email = props.getEmail().toLowerCase().trim();
        Optional<User> existing = userRepository.findByEmail(email);
        boolean created = existing.isEmpty();

        User user = existing.orElseGet(() ->
                userRepository.save(User.builder()
                    .email(email)
                    .name(props.getName())
                    .passwordHash(passwordEncoder.encode(props.getPassword()))
                    .planType(User.PlanType.FREE)
                    .role(UserRole.ADMIN)
                    .build())
        );

        // Garante login local (senha demo) mesmo quando o usuário nasceu via Supabase.
        user.setRole(UserRole.ADMIN);
        user.setPlanType(User.PlanType.FREE);
        user.setPasswordHash(passwordEncoder.encode(props.getPassword()));
        userRepository.save(user);

        // Garante que existe profile para esse usuário.
        UserProfile profile = profileRepository.findByUser_Id(user.getId())
                .orElseGet(() -> UserProfile.builder()
                        .user(user)
                        .completedOnboarding(true)
                        .build());
        profile.setCompletedOnboarding(true);
        profileRepository.save(profile);

        if (created) {
            log.info("Seed: criado admin local {} (login local /auth/login)", email);
        } else {
            log.info("Seed: admin existente atualizado {} (role=ADMIN)", email);
        }
    }

    private void seedUserIfMissing() {
        String email = props.getUserEmail().toLowerCase().trim();
        Optional<User> existing = userRepository.findByEmail(email);
        boolean created = existing.isEmpty();

        User user = existing.orElseGet(() ->
                userRepository.save(User.builder()
                    .email(email)
                    .name(props.getUserName())
                    .passwordHash(passwordEncoder.encode(props.getUserPassword()))
                    .planType(User.PlanType.FREE)
                    .role(UserRole.USER)
                    .build())
        );

        user.setRole(UserRole.USER);
        user.setPlanType(User.PlanType.FREE);
        user.setPasswordHash(passwordEncoder.encode(props.getUserPassword()));
        userRepository.save(user);

        UserProfile profile = profileRepository.findByUser_Id(user.getId())
                .orElseGet(() -> UserProfile.builder()
                        .user(user)
                        .completedOnboarding(props.isUserCompletedOnboarding())
                        .build());
        profile.setCompletedOnboarding(props.isUserCompletedOnboarding());
        profileRepository.save(profile);

        if (created) {
            log.info("Seed: criado usuário de teste {} (login local /auth/login)", email);
        } else {
            log.info("Seed: usuário existente atualizado {} (role=USER)", email);
        }
    }
}
