package com.mentoria.backend.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateSettingsRequest {

    private String language;

    @Min(value = 15, message = "Meta mínima é 15 minutos")
    @Max(value = 720, message = "Meta máxima é 720 minutos")
    private Integer dailyGoalMinutes;

    private String mentorStyle;

    private Boolean dailyReminder;
    private Boolean streakAlert;
    private Boolean planUpdate;
}
