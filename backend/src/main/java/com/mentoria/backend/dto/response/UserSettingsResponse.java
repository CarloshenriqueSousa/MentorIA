package com.mentoria.backend.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserSettingsResponse {

    @Builder.Default
    private String language = "pt-BR";

    @Builder.Default
    private Integer dailyGoalMinutes = 120;

    @Builder.Default
    private String mentorStyle = "balanced";

    @Builder.Default
    private Boolean dailyReminder = true;

    @Builder.Default
    private Boolean streakAlert = true;

    @Builder.Default
    private Boolean planUpdate = false;
}
