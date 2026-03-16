package com.mentoria.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class StudyProgressRequest {

    private LocalDate studyDate;

    @NotNull
    @Min(0) @Max(1440)
    private Integer minutesStudied;

    private List<String> topicsCompleted;

    @Min(0)
    private Integer exercisesDone;

    @Min(0)
    private Integer exercisesCorrect;

    @Min(1) @Max(5)
    private Integer moodRating;

    private String notes;

    private UUID studyPlanId;
}