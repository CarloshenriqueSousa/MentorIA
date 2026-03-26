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

import java.util.List;
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

        String prompt = """
                Atue como agente de pesquisa educacional.
                Tema: %s
                Retorne em português:
                1) Critérios de validação usados.
                2) Lista curada de materiais (livros, cursos, artigos, vídeos) com justificativa breve.
                """.formatted(topic);

        AiService.AIResponse response = aiService.generateResponse(prompt, List.of(), profile, user);

        return ResearchAgentResponse.builder()
                .topic(topic)
                .validatedCriteria("Autoridade da fonte, atualização, aderência ao objetivo e qualidade pedagógica.")
                .curatedMaterials(response.content())
                .build();
    }
}
