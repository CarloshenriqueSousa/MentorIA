package com.mentoria.backend.controller;

import com.mentoria.backend.dto.request.agents.LiaisonMessageRequest;
import com.mentoria.backend.dto.response.ApiResponse;
import com.mentoria.backend.dto.response.agents.LiaisonMessageResponse;
import com.mentoria.backend.dto.response.agents.PlannerAgentResponse;
import com.mentoria.backend.dto.response.agents.ResearchAgentResponse;
import com.mentoria.backend.model.StudyPlan;
import com.mentoria.backend.service.StudyPlanService;
import com.mentoria.backend.service.agents.LiaisonAgentService;
import com.mentoria.backend.service.agents.PlannerAgentService;
import com.mentoria.backend.service.agents.ResearchAgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AuthHelper authHelper;
    private final LiaisonAgentService liaisonAgentService;
    private final ResearchAgentService researchAgentService;
    private final PlannerAgentService plannerAgentService;
    private final StudyPlanService studyPlanService;

    @PostMapping("/liaison/message")
    public ResponseEntity<ApiResponse<LiaisonMessageResponse>> sendLiaisonMessage(
            @Valid @RequestBody LiaisonMessageRequest request
    ) {
        UUID userId = authHelper.getCurrentUserId();
        LiaisonMessageResponse response = liaisonAgentService.handleMessage(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/research/materials")
    public ResponseEntity<ApiResponse<ResearchAgentResponse>> findMaterials(
            @RequestParam String topic
    ) {
        UUID userId = authHelper.getCurrentUserId();
        ResearchAgentResponse response = researchAgentService.findValidatedMaterials(userId, topic);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/planner/guidance")
    public ResponseEntity<ApiResponse<PlannerAgentResponse>> getPlannerGuidance(
            @RequestParam String goal
    ) {
        UUID userId = authHelper.getCurrentUserId();
        PlannerAgentResponse response = plannerAgentService.buildGuidance(userId, goal);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @PostMapping("/planner/study-plan")
    public ResponseEntity<ApiResponse<StudyPlan>> generateStudyPlan() {
        UUID userId = authHelper.getCurrentUserId();
        StudyPlan plan = studyPlanService.generatePlan(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Plano gerado com sucesso!", plan));
    }
}
