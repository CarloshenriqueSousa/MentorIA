package com.mentoria.backend.dto.request;

<<<<<<< Updated upstream
import com.mentoria.backend.model.UserProfile;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class UpdateSettingsRequest {
    private String language;

    @Min(15)
    @Max(720)
    private Integer dailyGoalMinutes;

    private UserProfile.MentorStyle mentorStyle;

    private Map<String, Object> notifications;
=======
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
>>>>>>> Stashed changes
}
