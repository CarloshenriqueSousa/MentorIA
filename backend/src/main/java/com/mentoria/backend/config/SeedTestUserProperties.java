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

    // Segundo usuário (não-admin) apenas para facilitar a demo/testes locais.
    private String userEmail = "teste2@gmail.com";
    private String userPassword = "teste123";
    private String userName = "Usuário Teste 2";
    private boolean userCompletedOnboarding = true;

    // Lombok (@Data) costuma gerar esses acessores, mas adicionamos explicitamente
    // para evitar falsos positivos do linter/IDE quando Lombok não é reconhecido.
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public boolean isUserCompletedOnboarding() {
        return userCompletedOnboarding;
    }

    public void setUserCompletedOnboarding(boolean userCompletedOnboarding) {
        this.userCompletedOnboarding = userCompletedOnboarding;
    }
}
