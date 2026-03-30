package com.mentoria.backend.controller;

import com.mentoria.backend.dto.request.LoginRequest;
import com.mentoria.backend.dto.request.RegisterRequest;
import com.mentoria.backend.dto.request.SupabaseSessionRequest;
import com.mentoria.backend.dto.response.ApiResponse;
import com.mentoria.backend.dto.response.AuthResponse;
import com.mentoria.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Conta criada com sucesso", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login realizado", response));
    }

    @PostMapping("/supabase/session")
    public ResponseEntity<ApiResponse<AuthResponse>> supabaseSession(
            @Valid @RequestBody SupabaseSessionRequest request) {
        AuthResponse response = authService.syncSupabaseSession(
                request.getAccessToken(),
                request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.ok("Sessão sincronizada", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestHeader("X-Refresh-Token") String refreshToken) {
        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Logout realizado com sucesso")
                .build());
    }
}