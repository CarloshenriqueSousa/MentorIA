package com.mentoria.backend.dto.response.admin;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminAgentDto {

    private String id;
    private String name;
    private String description;
    /** IDLE, ONLINE, BUSY, ERROR */
    private String status;
    private String detail;
    /** Próximos passos / integração (ex.: ai-service, webhook) */
    private String integrationHint;
}
