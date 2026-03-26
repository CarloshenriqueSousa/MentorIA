package com.mentoria.backend.service.agents;

import com.mentoria.backend.dto.response.agents.PlannerAgentResponse;
import com.mentoria.backend.exception.ResourceNotFoundException;
import com.mentoria.backend.model.User;
import com.mentoria.backend.model.UserProfile;
import com.mentoria.backend.repository.UserProfileRepository;
import com.mentoria.backend.repository.UserRepository;
import com.mentoria.backend.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlannerAgentService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final AiService aiService;

    public PlannerAgentResponse buildGuidance(UUID userId, String goal) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        UserProfile profile = profileRepository.findByUser_Id(userId).orElse(null);

        // IMPORTANTE: o ai-service tem um system prompt de mentor conversacional.
        // Para tornar o retorno previsível, pedimos 3 blocos com delimitadores simples e fazemos parsing.
        String message = """
                Quero um plano de estudos para o seguinte objetivo: "%s".

                Responda SEMPRE em português do Brasil e SEM emojis.
                Retorne exatamente 3 blocos, nesta ordem, usando os delimitadores:
                ===CURSOS===
                (conteúdo)
                ===METODOS===
                (conteúdo)
                ===SEMANA===
                (conteúdo)

                Em CURSOS: recomende 2-5 cursos/trilhas e explique em 1 linha cada.
                Em METODOS: recomende métodos (ciclo, revisão espaçada, questões, simulados) e como aplicar.
                Em SEMANA: sugira uma estratégia semanal realista (de acordo com as horas/dia do perfil, se existir).
                """.formatted(goal);

        AiService.AIResponse response = aiService.generateResponse(message, List.of(), profile, user);
        String content = response.content() == null ? "" : response.content().trim();
        Map<String, String> sections = splitSections(content);
        String fallback = """
                IA indisponível no momento. Plano inicial para "%s":
                1) Cursos: escolha uma trilha principal + 1 curso de revisão por questões.
                2) Método: ciclos de estudo (50min foco + 10min pausa) e revisão 24h/7d.
                3) Semana: 5 blocos de teoria + 2 blocos de questões + 1 simulado curto.
                4) Ajuste: no fim da semana, revise erros e replaneje o próximo ciclo.
                """.formatted(goal);
        boolean useFallback = content.isBlank() || "fallback".equalsIgnoreCase(response.model());
        String courses = useFallback ? fallback : sections.getOrDefault("cursos", "");
        String methods = useFallback ? fallback : sections.getOrDefault("metodos", "");
        String week = useFallback ? fallback : sections.getOrDefault("semana", "");

        // Se o modelo ignorar os delimitadores, caímos para um texto único.
        if (!useFallback) {
            if (courses.isBlank()) courses = content;
            if (methods.isBlank()) methods = content;
            if (week.isBlank()) week = content;
        }

        return PlannerAgentResponse.builder()
                .goal(goal)
                .recommendedCourses(courses)
                .studyMethods(methods)
                .weeklyPlanGuidance(week)
                .build();
    }

    private Map<String, String> splitSections(String content) {
        String normalized = content == null ? "" : content;

        String courses = extractBetween(normalized, "===CURSOS===", "===METODOS===");
        String methods = extractBetween(normalized, "===METODOS===", "===SEMANA===");
        String week = extractAfter(normalized, "===SEMANA===");

        return Map.of(
                "cursos", trimSection(courses),
                "metodos", trimSection(methods),
                "semana", trimSection(week)
        );
    }

    private String extractBetween(String text, String startToken, String endToken) {
        int start = indexOfIgnoreCase(text, startToken);
        if (start < 0) return "";
        start += startToken.length();
        int end = indexOfIgnoreCase(text, endToken, start);
        if (end < 0) return "";
        return text.substring(start, end);
    }

    private String extractAfter(String text, String startToken) {
        int start = indexOfIgnoreCase(text, startToken);
        if (start < 0) return "";
        start += startToken.length();
        if (start > text.length()) return "";
        return text.substring(start);
    }

    private int indexOfIgnoreCase(String text, String token) {
        return indexOfIgnoreCase(text, token, 0);
    }

    private int indexOfIgnoreCase(String text, String token, int fromIndex) {
        if (text == null || token == null) return -1;
        return text.toLowerCase(Locale.ROOT).indexOf(token.toLowerCase(Locale.ROOT), Math.max(0, fromIndex));
    }

    private String trimSection(String s) {
        if (s == null) return "";
        String out = s.trim();
        while (out.startsWith("\n")) out = out.substring(1);
        while (out.endsWith("\n")) out = out.substring(0, out.length() - 1);
        return out.trim();
    }
}
