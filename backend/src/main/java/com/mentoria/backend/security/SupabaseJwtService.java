package com.mentoria.backend.security;

import com.mentoria.backend.config.supabase.SupabaseProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Valida access tokens emitidos pelo Supabase Auth (HS256 com o JWT Secret do projeto).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SupabaseJwtService {

    private final SupabaseProperties supabaseProperties;

    public boolean isConfigured() {
        return supabaseProperties.hasJwtSecret();
    }

    /**
     * Tenta validar assinatura e expiração. Retorna vazio se não configurado ou token inválido.
     */
    public Optional<Claims> tryParseValidClaims(String token) {
        if (!isConfigured()) {
            return Optional.empty();
        }
        try {
            SecretKey key = Keys.hmacShaKeyFor(
                    supabaseProperties.getJwtSecret().getBytes(StandardCharsets.UTF_8)
            );
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(claims);
        } catch (JwtException e) {
            log.debug("Supabase JWT não aceito: {}", e.getMessage());
            return Optional.empty();
        } catch (IllegalArgumentException e) {
            log.warn("SUPABASE_JWT_SECRET inválido para HMAC: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
