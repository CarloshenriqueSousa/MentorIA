package com.mentoria.backend.service.agents;

import com.mentoria.backend.dto.request.ChatMessageRequest;
import com.mentoria.backend.dto.request.agents.LiaisonMessageRequest;
import com.mentoria.backend.dto.response.agents.LiaisonMessageResponse;
import com.mentoria.backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LiaisonAgentService {

    private final ChatService chatService;

    public LiaisonMessageResponse handleMessage(UUID userId, LiaisonMessageRequest request) {
        String routedAgent = inferRoute(request.getContent());
        String routingReason = buildRoutingReason(routedAgent);

        ChatMessageRequest chatRequest = new ChatMessageRequest();
        chatRequest.setContent(request.getContent());
        chatRequest.setSessionId(request.getSessionId());

        return LiaisonMessageResponse.builder()
                .routedAgent(routedAgent)
                .routingReason(routingReason)
                .chat(chatService.sendMessage(userId, chatRequest))
                .build();
    }

    private String inferRoute(String content) {
        String normalized = content == null ? "" : content.toLowerCase(Locale.ROOT);

        if (containsAny(normalized, "plano", "cronograma", "método", "metodo", "curso")) {
            return "study_planner";
        }
        if (containsAny(normalized, "material", "fonte", "livro", "artigo", "validado")) {
            return "research";
        }
        return "liaison";
    }

    private String buildRoutingReason(String routedAgent) {
        return switch (routedAgent) {
            case "study_planner" -> "Mensagem classificada como solicitação de planejamento e método de estudo.";
            case "research" -> "Mensagem classificada como busca e validação de materiais.";
            default -> "Mensagem tratada no agente de orquestração e atendimento.";
        };
    }

    private boolean containsAny(String text, String... terms) {
        for (String term : terms) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }
}
