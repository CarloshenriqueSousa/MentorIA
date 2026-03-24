package com.mentoria.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupabaseSessionRequest {

    @NotBlank(message = "accessToken é obrigatório")
    private String accessToken;

    /**
     * Opcional; ecoado na resposta para o Pinia manter refresh (Supabase).
     */
    private String refreshToken;
}
