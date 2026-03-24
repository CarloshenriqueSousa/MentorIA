package com.mentoria.backend.dto.response.agents;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResearchAgentResponse {

    private String topic;
    private String validatedCriteria;
    private String curatedMaterials;
}
