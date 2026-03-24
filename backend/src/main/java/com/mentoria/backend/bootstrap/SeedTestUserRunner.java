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

/**
 * Cria usuário local de teste (admin) para login em {@code POST /auth/login}.
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
        String email = props.getEmail().toLowerCase().trim();
        if (userRepository.findByEmail(email).isPresent()) {
            log.debug("Seed: usuário {} já existe", email);
            return;
        }

        User user = User.builder()
                .email(email)
                .name(props.getName())
                .passwordHash(passwordEncoder.encode(props.getPassword()))
                .planType(User.PlanType.FREE)
                .role(UserRole.ADMIN)
                .build();
        user = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .completedOnboarding(true)
                .build();
        profileRepository.save(profile);

        log.info("Seed: criado usuário de teste admin {} (login local /auth/login)", email);
    }
}
