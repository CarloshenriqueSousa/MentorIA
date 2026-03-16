package com.mentoria.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChatMessageRequest {

    @NotBlank(message = "Mensagem não pode ser vazia")
    @Size(max = 4000, message = "Mensagem muito longa (máx 4000 caracteres)")
    private String content;

    private String sessionId;
}