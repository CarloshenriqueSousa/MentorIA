package com.mentoria.backend.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.mentoria.backend.model.ChatMessage;
import com.mentoria.backend.model.User;
import com.mentoria.backend.model.UserProfile;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AiService {

    private final WebClient aiWebClient;

    public AiService(@Qualifier("aiWebClient") WebClient aiWebClient) {
        this.aiWebClient = aiWebClient;
    }

    public AIResponse generateResponse(
            String userMessage,
            List<ChatMessage> history,
            UserProfile profile,
            User user
    ) {
        Map<String, Object> body = buildRequestBody(userMessage, history, profile, user);

        try {
            Map response = aiWebClient.post()
                    .uri("/generate")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();

            if (response == null) throw new RuntimeException("Resposta vazia da IA");

            return new AIResponse(
                    (String) response.get("content"),
                    ((Number) response.getOrDefault("tokens_used", 0)).intValue(),
                    (String) response.getOrDefault("model", "grok")
            );

        } catch (WebClientResponseException ex) {
            log.error("AI Service error {}: {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            return fallbackResponse();
        } catch (Exception ex) {
            log.error("AI Service falhou: {}", ex.getMessage());
            return fallbackResponse();
        }
    }

    public Map<String, Object> generateStudyPlan(UserProfile profile, User user) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", user.getId().toString());
        body.put("profile", buildProfileContext(profile));

        try {
            Map response = aiWebClient.post()
                    .uri("/study-plan")
                    .bodyValue(body)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(90))
                    .block();

            if (response == null) throw new RuntimeException("Plano vazio retornado");
            return response;

        } catch (Exception ex) {
            log.error("Geração de plano falhou: {}", ex.getMessage());
            throw new RuntimeException("Erro ao gerar plano de estudos: " + ex.getMessage());
        }
    }

    private Map<String, Object> buildRequestBody(
            String userMessage,
            List<ChatMessage> history,
            UserProfile profile,
            User user
    ) {
        Map<String, Object> body = new HashMap<>();
        body.put("user_id", user.getId().toString());
        body.put("message", userMessage);

        List<Map<String, String>> historyList = history.stream()
                .filter(m -> m.getRole() != ChatMessage.Role.SYSTEM)
                .map(m -> Map.of("role", m.getRole().name().toLowerCase(), "content", m.getContent()))
                .collect(Collectors.toList());
        body.put("history", historyList);

        if (profile != null) {
            body.put("profile", buildProfileContext(profile));
        }

        return body;
    }

    private Map<String, Object> buildProfileContext(UserProfile profile) {
        Map<String, Object> ctx = new HashMap<>();
        if (profile.getTargetExam() != null) ctx.put("target_exam", profile.getTargetExam());
        if (profile.getObjectives() != null) ctx.put("objectives", profile.getObjectives());
        if (profile.getDifficulties() != null) ctx.put("difficulties", profile.getDifficulties());
        if (profile.getStrengths() != null) ctx.put("strengths", profile.getStrengths());
        if (profile.getKnowledgeLevel() != null) ctx.put("knowledge_level", profile.getKnowledgeLevel().name());
        if (profile.getStudyHoursPerDay() != null) ctx.put("study_hours_per_day", profile.getStudyHoursPerDay());
        return ctx;
    }

    private AIResponse fallbackResponse() {
        return new AIResponse(
                "Desculpe, estou com dificuldades técnicas no momento. Tente novamente em alguns instantes.",
                0,
                "fallback"
        );
    }

    public record AIResponse(String content, int tokensUsed, String model) {}
}