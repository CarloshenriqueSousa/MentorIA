package com.mentoria.backend.dto.response;

import com.mentoria.backend.model.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private UserSummary user;

    @Data
    @Builder
    public static class UserSummary {
        private UUID id;
        private String name;
        private String email;
        private User.PlanType planType;
        private boolean completedOnboarding;
        private LocalDateTime createdAt;
    }
}