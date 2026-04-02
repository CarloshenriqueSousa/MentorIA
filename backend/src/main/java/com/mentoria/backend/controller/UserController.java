package com.mentoria.backend.controller;

import com.mentoria.backend.dto.request.UpdateProfileRequest;
import com.mentoria.backend.dto.request.UpdateSettingsRequest;
import com.mentoria.backend.dto.response.ApiResponse;
import com.mentoria.backend.dto.response.UserSettingsResponse;
import com.mentoria.backend.model.User;
import com.mentoria.backend.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthHelper authHelper;

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UUID userId = authHelper.getCurrentUserId();
        User user = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Perfil atualizado", Map.of(
                "id", user.getId().toString(),
                "name", user.getName(),
                "email", user.getEmail()
        )));
    }

    @GetMapping("/me/settings")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> getSettings() {
        UUID userId = authHelper.getCurrentUserId();
        UserSettingsResponse settings = userService.getSettings(userId);
        return ResponseEntity.ok(ApiResponse.ok(settings));
    }

    @PutMapping("/me/settings")
    public ResponseEntity<ApiResponse<UserSettingsResponse>> updateSettings(
            @Valid @RequestBody UpdateSettingsRequest request) {
        UUID userId = authHelper.getCurrentUserId();
        UserSettingsResponse settings = userService.updateSettings(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Configurações salvas", settings));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        UUID userId = authHelper.getCurrentUserId();
        userService.deleteAccount(userId);
        return ResponseEntity.ok(ApiResponse.ok("Conta excluída com sucesso", null));
    }
}
