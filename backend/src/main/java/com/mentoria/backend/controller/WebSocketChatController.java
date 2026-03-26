package com.mentoria.backend.controller;

import com.mentoria.backend.dto.request.ChatMessageRequest;
import com.mentoria.backend.dto.response.ChatResponse;
import com.mentoria.backend.model.User;
import com.mentoria.backend.repository.UserRepository;
import com.mentoria.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class WebSocketChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        if (principal == null) {
            log.error("Tentativa de enviar mensagem sem autenticação via WebSocket");
            return;
        }

        UUID userId;
        if (principal instanceof UsernamePasswordAuthenticationToken) {
            // No MentorIA, o Principal geralmente contém o email ou ID
            String username = principal.getName();
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + username));
            userId = user.getId();
        } else {
            // Em caso de custom principal
            userId = UUID.fromString(principal.getName());
        }

        try {
            ChatResponse response = chatService.sendMessage(userId, request);

            // Envia para o tópico da sessão específica
            messagingTemplate.convertAndSend("/topic/chat/" + response.getSessionId(), response);
        } catch (Exception e) {
            log.error("Erro ao processar mensagem via WebSocket", e);
            // Poderia enviar um erro via /queue/errors
        }
    }
}
