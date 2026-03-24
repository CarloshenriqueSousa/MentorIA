package com.mentoria.backend.service;

import com.mentoria.backend.dto.response.admin.AdminAgentDto;
import com.mentoria.backend.dto.response.admin.AdminOverviewDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Visão geral dos agentes de IA (orquestração). Status mock até integrar filas/health por agente.
 */
@Service
public class AdminService {

    private final Environment environment;

    @Value("${spring.application.name:mentoria-backend}")
    private String applicationName;

    public AdminService(Environment environment) {
        this.environment = environment;
    }

    public AdminOverviewDto getOverview() {
        String env = List.of(environment.getActiveProfiles()).isEmpty()
                ? "default"
                : String.join(",", environment.getActiveProfiles());

        List<AdminAgentDto> agents = List.of(
                AdminAgentDto.builder()
                        .id("research")
                        .name("Agente de pesquisa")
                        .description("Busca e valida materiais de estudo (fontes, qualidade, alinhamento ao edital).")
                        .status("IDLE")
                        .detail("Pronto para enfileirar tarefas de curadoria de conteúdo.")
                        .integrationHint("Conectar ao ai-service / pipeline de RAG quando disponível.")
                        .build(),
                AdminAgentDto.builder()
                        .id("liaison")
                        .name("Agente de orquestração")
                        .description("Faz a ponte entre o usuário, o backend e os demais agentes (sessão, contexto, permissões).")
                        .status("ONLINE")
                        .detail("Sincronizado com autenticação e perfil local.")
                        .integrationHint("WebSocket / eventos Spring + fila de mensagens do chat.")
                        .build(),
                AdminAgentDto.builder()
                        .id("lesson_planner")
                        .name("Agente de planos de aula")
                        .description("Monta planos de estudo e sequência didática personalizada.")
                        .status("IDLE")
                        .detail("Aguardando integração com study-plans e modelo Claude.")
                        .integrationHint("ai-service + StudyPlanService.")
                        .build()
        );

        return AdminOverviewDto.builder()
                .environment(env + " (" + applicationName + ")")
                .agents(agents)
                .build();
    }
}
