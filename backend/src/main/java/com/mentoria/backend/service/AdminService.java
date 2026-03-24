package com.mentoria.backend.service;

import com.mentoria.backend.dto.response.admin.AdminAgentDto;
import com.mentoria.backend.dto.response.admin.AdminOverviewDto;
import com.mentoria.backend.security.SupabaseJwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminService {

    private final Environment environment;
    private final AiService aiService;
    private final SupabaseJwtService supabaseJwtService;

    @Value("${spring.application.name:mentoria-backend}")
    private String applicationName;

    public AdminService(
            Environment environment,
            AiService aiService,
            SupabaseJwtService supabaseJwtService
    ) {
        this.environment = environment;
        this.aiService = aiService;
        this.supabaseJwtService = supabaseJwtService;
    }

    public AdminOverviewDto getOverview() {
        String env = List.of(environment.getActiveProfiles()).isEmpty()
                ? "default"
                : String.join(",", environment.getActiveProfiles());
        boolean aiHealthy = aiService.isHealthy();
        boolean supabaseJwtReady = supabaseJwtService.isConfigured();

        List<AdminAgentDto> agents = List.of(
                AdminAgentDto.builder()
                        .id("research")
                        .name("Agente de pesquisa")
                        .description("Busca e valida materiais de estudo (fontes, qualidade, alinhamento ao edital).")
                        .status(aiHealthy ? "ONLINE" : "ERROR")
                        .detail(aiHealthy
                                ? "Conectado ao AI Service para curadoria e validação de materiais."
                                : "AI Service indisponível. A pesquisa automática está pausada.")
                        .integrationHint("Integração com RAG/recuperação semântica pode ser ligada no próximo passo.")
                        .build(),
                AdminAgentDto.builder()
                        .id("liaison")
                        .name("Agente de orquestração")
                        .description("Faz a ponte entre o usuário, o backend e os demais agentes (sessão, contexto, permissões).")
                        .status(supabaseJwtReady ? "ONLINE" : "BUSY")
                        .detail(supabaseJwtReady
                                ? "Autenticação local e Supabase prontas para sincronização de sessão."
                                : "Autenticação local ativa; falta SUPABASE_JWT_SECRET para validar tokens Supabase no backend.")
                        .integrationHint("Fluxo principal: /auth/login e /auth/supabase/session.")
                        .build(),
                AdminAgentDto.builder()
                        .id("study_planner")
                        .name("Agente de planejamento")
                        .description("Cria planos de estudo, cursos recomendados e métodos de aprendizagem personalizados.")
                        .status(aiHealthy ? "ONLINE" : "IDLE")
                        .detail(aiHealthy
                                ? "Pronto para gerar planos via StudyPlanService + AI Service."
                                : "Aguardando AI Service para montar planos personalizados.")
                        .integrationHint("Endpoints de estudo já disponíveis em /study-plans.")
                        .build()
        );

        return AdminOverviewDto.builder()
                .environment(env + " (" + applicationName + ")")
                .agents(agents)
                .build();
    }
}
