package com.mentoria.backend.exception;

/**
 * Erros de autenticação que devem retornar HTTP 401 (ex.: token Supabase inválido).
 */
public class ApiUnauthorizedException extends RuntimeException {

    public ApiUnauthorizedException(String message) {
        super(message);
    }
}
