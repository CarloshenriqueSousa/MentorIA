package com.mentoria.backend.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.seed.test-user")
public class SeedTestUserProperties {

    /**
     * Desative em produção ({@code application-prod.yml}).
     */
    private boolean enabled = true;

    private String email = "teste@gmail.com";
    private String password = "teste123";
    private String name = "Usuário Teste";
}
