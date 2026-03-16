package com.mentoria.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotBlank
    @Pattern(regexp = "BASIC|PREMIUM", message = "Plano deve ser BASIC ou PREMIUM")
    private String plan;

    private String successUrl;
    private String cancelUrl;
}