package com.mentoria.backend.controller;

import com.mentoria.backend.dto.request.StudyProgressRequest;
import com.mentoria.backend.dto.response.ApiResponse;
import com.mentoria.backend.dto.response.DashboardResponse;
import com.mentoria.backend.model.StudyProgress;
import com.mentoria.backend.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final AuthHelper authHelper;

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        UUID userId = authHelper.getCurrentUserId();
        DashboardResponse dashboard = dashboardService.getDashboard(userId);
        return ResponseEntity.ok(ApiResponse.ok(dashboard));
    }

    @PostMapping("/progress")
    public ResponseEntity<ApiResponse<StudyProgress>> logProgress(
            @Valid @RequestBody StudyProgressRequest request) {
        UUID userId = authHelper.getCurrentUserId();
        StudyProgress progress = dashboardService.logProgress(userId, request);
        return ResponseEntity.ok(ApiResponse.ok("Progresso registrado! +XP", progress));
    }
}