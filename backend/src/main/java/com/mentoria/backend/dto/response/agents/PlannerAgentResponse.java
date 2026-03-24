package com.mentoria.backend.dto.response.agents;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PlannerAgentResponse {

    private String goal;
    private String recommendedCourses;
    private String studyMethods;
    private String weeklyPlanGuidance;
}
