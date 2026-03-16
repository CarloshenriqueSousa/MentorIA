package com.mentoria.backend.dto.request;

import com.mentoria.backend.model.UserProfile;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class OnboardingRequest {

    @NotBlank(message = "Concurso alvo é obrigatório")
    private String targetExam;

    private UserProfile.KnowledgeLevel knowledgeLevel;

    @Min(1) @Max(16)
    private Integer studyHoursPerDay;

    @NotEmpty
    private List<String> availableDays;

    @NotEmpty
    private List<String> objectives;

    private List<String> difficulties;

    private List<String> strengths;

    private List<Map<String, Object>> subjectsPriority;

    private UserProfile.LearningStyle learningStyle;

    private Map<String, Object> extraInfo;
}