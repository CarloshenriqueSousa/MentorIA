package com.mentoria.backend.controller;

import com.mentoria.backend.dto.request.ChatMessageRequest;
import com.mentoria.backend.dto.response.ApiResponse;
import com.mentoria.backend.dto.response.ChatResponse;
import com.mentoria.backend.model.ChatMessage;
import com.mentoria.backend.model.ChatSession;
import com.mentoria.backend.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final AuthHelper authHelper;

    @PostMapping("/message")
    public ResponseEntity<ApiResponse<ChatResponse>> sendMessage(
            @Valid @RequestBody ChatMessageRequest request) {
        UUID userId = authHelper.getCurrentUserId();
        ChatResponse response = chatService.sendMessage(userId, request);
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ChatSession>>> getSessions() {
        UUID userId = authHelper.getCurrentUserId();
        List<ChatSession> sessions = chatService.getUserSessions(userId);
        return ResponseEntity.ok(ApiResponse.ok(sessions));
    }

    @GetMapping("/sessions/{sessionId}/messages")
    public ResponseEntity<ApiResponse<List<ChatMessage>>> getMessages(
            @PathVariable UUID sessionId) {
        UUID userId = authHelper.getCurrentUserId();
        List<ChatMessage> messages = chatService.getSessionMessages(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.ok(messages));
    }

    @DeleteMapping("/sessions/{sessionId}")
    public ResponseEntity<ApiResponse<Void>> deleteSession(
            @PathVariable UUID sessionId) {
        UUID userId = authHelper.getCurrentUserId();
        chatService.deleteSession(userId, sessionId);
        return ResponseEntity.ok(ApiResponse.ok("Sessão excluída", null));
    }
}