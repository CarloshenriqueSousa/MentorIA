package com.mentoria.backend.controller;

import com.mentoria.backend.dto.request.ChangePasswordRequest;
import com.mentoria.backend.dto.request.UpdateProfileRequest;
import com.mentoria.backend.dto.request.UpdateSettingsRequest;
import com.mentoria.backend.dto.response.ApiResponse;
import com.mentoria.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthHelper authHelper;

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = authHelper.getCurrentUserId();
        userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Perfil atualizado com sucesso"));
    }

    @PutMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request) {
        UUID userId = authHelper.getCurrentUserId();
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Senha alterada com sucesso"));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<Void>> updateSettings(
            @Valid @RequestBody UpdateSettingsRequest request) {
        UUID userId = authHelper.getCurrentUserId();
        userService.updateSettings(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Configurações atualizadas com sucesso"));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<UpdateSettingsRequest>> getSettings() {
        UUID userId = authHelper.getCurrentUserId();
        UpdateSettingsRequest settings = userService.getSettings(userId);
        return ResponseEntity.ok(ApiResponse.ok(settings));
    }
}
