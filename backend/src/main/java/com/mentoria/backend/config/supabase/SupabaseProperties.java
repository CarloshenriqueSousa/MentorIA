package com.mentoria.backend.config.supabase;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Credenciais do projeto Supabase (API REST / futuras integrações).
 * <p>
 * Use {@code key} com a chave <strong>publishable/anon</strong> apenas para chamadas que respeitam RLS.
 * Use {@code serviceRoleKey} só no servidor, nunca no frontend — operações administrativas.
 */
@Data
@ConfigurationProperties(prefix = "supabase")
public class SupabaseProperties {

    /**
     * Ex.: https://&lt;project-ref&gt;.supabase.co
     */
    private String url = "";

    /**
     * Chave publishable (sb_publishable_...) ou anon legada.
     */
    private String key = "";

    /**
     * Service role — opcional; necessária para bypass de RLS em jobs/admin.
     */
    private String serviceRoleKey = "";

    /**
     * JWT Secret do projeto (Settings → API → JWT Secret). Obrigatório para validar access tokens do Supabase Auth no backend.
     */
    private String jwtSecret = "";

    public boolean hasJwtSecret() {
        return jwtSecret != null && !jwtSecret.isBlank();
    }
}
