package com.mentoria.backend.controller;

import com.mentoria.backend.dto.response.ApiResponse;
import com.mentoria.backend.model.StudyPlan;
import com.mentoria.backend.service.StudyPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/study-plans")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;
    private final AuthHelper authHelper;

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<StudyPlan>> generatePlan() {
        UUID userId = authHelper.getCurrentUserId();
        StudyPlan plan = studyPlanService.generatePlan(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Plano gerado com sucesso!", plan));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<StudyPlan>>> getPlans() {
        UUID userId = authHelper.getCurrentUserId();
        List<StudyPlan> plans = studyPlanService.getUserPlans(userId);
        return ResponseEntity.ok(ApiResponse.ok(plans));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<StudyPlan>> getActivePlan() {
        UUID userId = authHelper.getCurrentUserId();
        StudyPlan plan = studyPlanService.getActivePlan(userId);
        return ResponseEntity.ok(ApiResponse.ok(plan));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<StudyPlan>> getPlan(
            @PathVariable UUID planId) {
        UUID userId = authHelper.getCurrentUserId();
        StudyPlan plan = studyPlanService.getPlan(userId, planId);
        return ResponseEntity.ok(ApiResponse.ok(plan));
    }

    @PatchMapping("/{planId}/status")
    public ResponseEntity<ApiResponse<StudyPlan>> updateStatus(
            @PathVariable UUID planId,
            @RequestParam StudyPlan.PlanStatus status) {
        UUID userId = authHelper.getCurrentUserId();
        StudyPlan plan = studyPlanService.updateStatus(userId, planId, status);
        return ResponseEntity.ok(ApiResponse.ok("Status atualizado", plan));
    }
}