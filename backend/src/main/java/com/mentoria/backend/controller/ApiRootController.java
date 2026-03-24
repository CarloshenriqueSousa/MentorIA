package com.mentoria.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Resposta em {@code GET /api/} — a raiz {@code http://localhost:8080/} fica fora do contexto
 * da aplicação (Tomcat não mapeia nada em {@code /}), por isso costuma dar 404.
 */
@RestController
public class ApiRootController {

    @GetMapping("/")
    public Map<String, Object> apiRoot() {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", "MentorIA API");
        body.put("status", "running");
        body.put("health", "/actuator/health");
        body.put("auth", "/auth");
        body.put("note", "A API está sob o prefixo /api. Use http://localhost:8080/api/... "
                + "A interface web (Nuxt) roda em outra porta (ex.: 3000 ou 3001), não no 8080.");
        return body;
    }
}
