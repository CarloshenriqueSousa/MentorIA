package com.mentoria.backend.service;

import com.mentoria.backend.dto.request.LoginRequest;
import com.mentoria.backend.dto.request.RegisterRequest;
import com.mentoria.backend.dto.response.AuthResponse;
import com.mentoria.backend.exception.BusinessException;
import com.mentoria.backend.model.User;
import com.mentoria.backend.model.UserProfile;
import com.mentoria.backend.repository.UserProfileRepository;
import com.mentoria.backend.repository.UserRepository;
import com.mentoria.backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail().toLowerCase())) {
            throw new BusinessException("Email já cadastrado");
        }

        User user = User.builder()
                .name(request.getName().trim())
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .planType(User.PlanType.FREE)
                .build();

        user = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .completedOnboarding(false)
                .build();
        profileRepository.save(profile);

        log.info("Novo usuário registrado: {}", user.getEmail());
        return buildAuthResponse(user, false);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase(),
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(request.getEmail().toLowerCase())
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        userRepository.updateLastLogin(user.getId(), LocalDateTime.now());

        boolean completedOnboarding = profileRepository.findByUser_Id(user.getId())
                .map(UserProfile::isCompletedOnboarding)
                .orElse(false);

        log.info("Login realizado: {}", user.getEmail());
        return buildAuthResponse(user, completedOnboarding);
    }

    public AuthResponse refreshToken(String refreshToken) {
        String email = jwtTokenProvider.extractUsername(refreshToken);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if (!jwtTokenProvider.isTokenValid(refreshToken, userDetails)) {
            throw new BusinessException("Token inválido ou expirado");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuário não encontrado"));

        boolean completedOnboarding = profileRepository.findByUser_Id(user.getId())
                .map(UserProfile::isCompletedOnboarding)
                .orElse(false);

        return buildAuthResponse(user, completedOnboarding);
    }

    private AuthResponse buildAuthResponse(User user, boolean completedOnboarding) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String accessToken = jwtTokenProvider.generateToken(userDetails, user.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(86400)
                .user(AuthResponse.UserSummary.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .planType(user.getPlanType())
                        .completedOnboarding(completedOnboarding)
                        .createdAt(user.getCreatedAt())
                        .build())
                .build();
    }
}