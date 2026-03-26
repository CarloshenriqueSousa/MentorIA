package com.mentoria.backend.service.agents;

import com.mentoria.backend.dto.response.agents.ResearchAgentResponse;
import com.mentoria.backend.exception.ResourceNotFoundException;
import com.mentoria.backend.model.User;
import com.mentoria.backend.model.UserProfile;
import com.mentoria.backend.repository.UserProfileRepository;
import com.mentoria.backend.repository.UserRepository;
import com.mentoria.backend.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResearchAgentService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final AiService aiService;

    public ResearchAgentResponse findValidatedMaterials(UUID userId, String topic) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        UserProfile profile = profileRepository.findByUser_Id(userId).orElse(null);

        // IMPORTANTE: o ai-service já tem heurística + ferramenta de busca para "materiais/links/pdf".
        // Para aproveitar isso, enviamos uma mensagem natural (em vez de "Atue como agente...").
        String message = """
                Preciso de materiais de estudo validados e com links sobre o tema: "%s".
                Entregue uma lista curada (livros, cursos, artigos, vídeos e bancos de questões) com justificativa breve.
                Se possível, priorize fontes oficiais e conteúdos atualizados.
                """.formatted(topic);

        AiService.AIResponse response = aiService.generateResponse(message, java.util.List.of(), profile, user);

        return ResearchAgentResponse.builder()
                .topic(topic)
                .validatedCriteria("Autoridade da fonte, atualização, aderência ao objetivo e qualidade pedagógica.")
                .curatedMaterials(resolveCuratedMaterials(topic, response))
                .build();
    }

    private String resolveCuratedMaterials(String topic, AiService.AIResponse response) {
        String content = response.content() == null ? "" : response.content().trim();
        if (!content.isEmpty() && !"fallback".equalsIgnoreCase(response.model())) {
            return content;
        }

        return """
                IA indisponível no momento. Sugestão inicial validada para "%s":

                - Livro-base: coleção didática reconhecida no tema, com exercícios resolvidos.
                - Curso estruturado: trilha com módulos progressivos e revisão semanal.
                - Banco de questões: priorize questões comentadas de provas recentes.
                - Vídeos de revisão: use aulas curtas para reforço de pontos fracos.

                Critérios aplicados: fonte confiável, atualização recente, aderência ao objetivo e clareza pedagógica.
                """.formatted(topic);
    }
}
