package com.mentoria.backend.controller;

import com.mentoria.backend.dto.request.OnboardingRequest;
import com.mentoria.backend.dto.response.ApiResponse;
import com.mentoria.backend.model.UserProfile;
import com.mentoria.backend.service.OnboardingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/onboarding")
@RequiredArgsConstructor
public class OnboardingController {

    private final OnboardingService onboardingService;
    private final AuthHelper authHelper;

    @PostMapping
    public ResponseEntity<ApiResponse<UserProfile>> completeOnboarding(
            @Valid @RequestBody OnboardingRequest request) {
        UUID userId = authHelper.getCurrentUserId();
        UserProfile profile = onboardingService.saveOnboarding(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Onboarding concluído!", profile));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<UserProfile>> getProfile() {
        UUID userId = authHelper.getCurrentUserId();
        UserProfile profile = onboardingService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.ok(profile));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserProfile>> updateProfile(
            @Valid @RequestBody OnboardingRequest request) {
        UUID userId = authHelper.getCurrentUserId();
        UserProfile profile = onboardingService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Perfil atualizado", profile));
    }
}