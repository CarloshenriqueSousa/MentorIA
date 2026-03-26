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

        String prompt = """
                Atue como agente planejador de estudos.
                Objetivo do aluno: %s
                Responda em português com:
                - cursos recomendados
                - métodos de estudo recomendados
                - estratégia semanal resumida
                """.formatted(goal);

        AiService.AIResponse response = aiService.generateResponse(prompt, List.of(), profile, user);
        String content = response.content();

        return PlannerAgentResponse.builder()
                .goal(goal)
                .recommendedCourses(content)
                .studyMethods(content)
                .weeklyPlanGuidance(content)
                .build();
    }
}
