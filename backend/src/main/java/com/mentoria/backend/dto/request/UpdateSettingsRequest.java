package com.mentoria.backend.dto.request;

import com.mentoria.backend.model.UserProfile;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import java.util.Map;

@Data
public class UpdateSettingsRequest {
    private String language;

    @Min(15)
    @Max(720)
    private Integer dailyGoalMinutes;

    private UserProfile.MentorStyle mentorStyle;

    private Map<String, Object> notifications;
}
