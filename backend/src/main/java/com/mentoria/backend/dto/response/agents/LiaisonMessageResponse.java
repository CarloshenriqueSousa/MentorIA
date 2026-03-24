package com.mentoria.backend.dto.response.agents;

import com.mentoria.backend.dto.response.ChatResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LiaisonMessageResponse {

    private String routedAgent;
    private String routingReason;
    private ChatResponse chat;
}
