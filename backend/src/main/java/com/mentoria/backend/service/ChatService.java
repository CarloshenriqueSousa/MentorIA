package com.mentoria.backend.service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mentoria.backend.dto.request.ChatMessageRequest;
import com.mentoria.backend.dto.response.ChatResponse;
import com.mentoria.backend.exception.RateLimitException;
import com.mentoria.backend.exception.ResourceNotFoundException;
import com.mentoria.backend.model.ChatMessage;
import com.mentoria.backend.model.ChatSession;
import com.mentoria.backend.model.MessageUsage;
import com.mentoria.backend.model.User;
import com.mentoria.backend.model.UserProfile;
import com.mentoria.backend.repository.ChatMessageRepository;
import com.mentoria.backend.repository.ChatSessionRepository;
import com.mentoria.backend.repository.MessageUsageRepository;
import com.mentoria.backend.repository.UserProfileRepository;
import com.mentoria.backend.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {

    private final ChatSessionRepository sessionRepository;
    private final ChatMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final MessageUsageRepository usageRepository;
    private final AiService aiService;

    @Value("${freemium.daily-message-limit}")
    private int dailyMessageLimit;

    @Transactional
    public ChatResponse sendMessage(UUID userId, ChatMessageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        int remaining = checkFreemiumLimit(user);

        ChatSession session = resolveSession(userId, request.getSessionId());

        ChatMessage userMessage = ChatMessage.builder()
                .session(session)
                .role(ChatMessage.Role.USER)
                .content(request.getContent())
                .build();
        messageRepository.save(userMessage);

        List<ChatMessage> history = messageRepository.findLastNBySessionId(
                session.getId(), PageRequest.of(0, 20)
        );
        Collections.reverse(history);

        UserProfile profile = profileRepository.findByUser_Id(userId).orElse(null);

        AiService.AIResponse aiResponse = aiService.generateResponse(
                request.getContent(), history, profile, user
        );

        ChatMessage assistantMessage = ChatMessage.builder()
                .session(session)
                .role(ChatMessage.Role.ASSISTANT)
                .content(aiResponse.content())
                .tokensUsed(aiResponse.tokensUsed())
                .modelUsed(aiResponse.model())
                .build();
        messageRepository.save(assistantMessage);

        session.setMessageCount(session.getMessageCount() + 2);
        if (session.getMessageCount() == 2) {
            session.setTitle(request.getContent().length() <= 50
                    ? request.getContent()
                    : request.getContent().substring(0, 47) + "...");
        }
        sessionRepository.save(session);

        MessageUsage usage = usageRepository.findByUser_IdAndUsageDate(userId, LocalDate.now())
                .orElse(MessageUsage.builder().user(user).usageDate(LocalDate.now()).build());
        usage.setMessageCount(usage.getMessageCount() + 1);
        usage.setTokensUsed(usage.getTokensUsed() + aiResponse.tokensUsed());
        usageRepository.save(usage);

        int newRemaining = Math.max(0, remaining - 1);

        return ChatResponse.builder()
                .sessionId(session.getId())
                .sessionTitle(session.getTitle())
                .assistantMessage(ChatResponse.MessageDto.builder()
                        .id(assistantMessage.getId())
                        .role(assistantMessage.getRole())
                        .content(assistantMessage.getContent())
                        .createdAt(assistantMessage.getCreatedAt())
                        .build())
                .limitReached(newRemaining == 0)
                .remainingMessages(newRemaining)
                .build();
    }

    public List<ChatSession> getUserSessions(UUID userId) {
        return sessionRepository.findByUser_IdAndIsActiveTrueOrderByUpdatedAtDesc(userId);
    }

    public List<ChatMessage> getSessionMessages(UUID userId, UUID sessionId) {
        sessionRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada"));
        return messageRepository.findBySession_IdOrderByCreatedAtAsc(sessionId);
    }

    @Transactional
    public void deleteSession(UUID userId, UUID sessionId) {
        ChatSession session = sessionRepository.findByIdAndUser_Id(sessionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada"));
        session.setActive(false);
        sessionRepository.save(session);
    }

    private int checkFreemiumLimit(User user) {
        if (user.getPlanType() != User.PlanType.FREE) return Integer.MAX_VALUE;

        int current = usageRepository.findByUser_IdAndUsageDate(user.getId(), LocalDate.now())
                .map(MessageUsage::getMessageCount)
                .orElse(0);

        int remaining = dailyMessageLimit - current;
        if (remaining <= 0) {
            throw new RateLimitException(
                    "Você atingiu o limite de " + dailyMessageLimit + " mensagens diárias. Faça upgrade!", 0
            );
        }
        return remaining;
    }

    private ChatSession resolveSession(UUID userId, String sessionIdStr) {
        if (sessionIdStr != null && !sessionIdStr.isBlank()) {
            return sessionRepository.findByIdAndUser_Id(UUID.fromString(sessionIdStr), userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sessão não encontrada"));
        }
        User user = userRepository.getReferenceById(userId);
        return sessionRepository.save(ChatSession.builder().user(user).build());
    }
}