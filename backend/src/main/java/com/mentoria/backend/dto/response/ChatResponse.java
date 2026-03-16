package com.mentoria.backend.dto.response;

import com.mentoria.backend.model.ChatMessage;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ChatResponse {

    private UUID sessionId;
    private String sessionTitle;
    private MessageDto assistantMessage;
    private boolean limitReached;
    private int remainingMessages;

    @Data
    @Builder
    public static class MessageDto {
        private UUID id;
        private ChatMessage.Role role;
        private String content;
        private LocalDateTime createdAt;
    }
}