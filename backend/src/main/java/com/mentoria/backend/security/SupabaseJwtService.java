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
import java.security.MessageDigest;

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

    private byte[] resolveHmacKeyBytes() {
        // O "JWT Secret" do Supabase geralmente vem em Base64. Para HS256,
        // usamos os bytes decodificados quando isso parece válido.
        String secret = supabaseProperties.getJwtSecret();
        if (secret == null) secret = "";
        secret = secret.trim();
        if (secret.isEmpty()) return new byte[0];

        String normalized = secret.replaceAll("\\s+", "");

        // Base64 padrão
        try {
            byte[] decoded = java.util.Base64.getDecoder().decode(normalized);
            if (decoded.length >= 32) {
                return decoded;
            }
        } catch (IllegalArgumentException ignored) {
            // not base64 (standard)
        }

        // Base64 URL-safe
        try {
            byte[] decoded = java.util.Base64.getUrlDecoder().decode(normalized);
            if (decoded.length >= 32) {
                return decoded;
            }
        } catch (IllegalArgumentException ignored) {
            // not base64 (url)
        }

        // Fallback: secret como string raw
        byte[] rawBytes = secret.getBytes(StandardCharsets.UTF_8);
        if (rawBytes.length >= 32) {
            return rawBytes;
        }

        // Garante tamanho >=256 bits (>=32 bytes) para HS256
        return sha256(rawBytes);
    }

    private static byte[] sha256(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input);
        } catch (java.security.NoSuchAlgorithmException e) {
            return new byte[32];
        }
    }

    /**
     * Tenta validar assinatura e expiração. Retorna vazio se não configurado ou token inválido.
     */
    public Optional<Claims> tryParseValidClaims(String token) {
        if (!isConfigured()) {
            return Optional.empty();
        }
        try {
            byte[] keyBytes = resolveHmacKeyBytes();
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);
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
