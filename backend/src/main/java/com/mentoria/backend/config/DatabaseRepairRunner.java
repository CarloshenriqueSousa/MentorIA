package com.mentoria.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Reparos idempotentes para dev/local.
 *
 * O projeto usa {@code spring.jpa.hibernate.ddl-auto=update}. Em bancos com schema antigo,
 * o Hibernate pode falhar ao aplicar constraints NOT NULL (ex.: coluna nova sem default).
 * Este runner garante defaults mínimos para evitar crash no startup.
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class DatabaseRepairRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;
    private final Environment environment;

    @Override
    public void run(ApplicationArguments args) {
        // Evita rodar em prod por precaução (ainda assim é idempotente).
        for (String profile : environment.getActiveProfiles()) {
            if ("prod".equalsIgnoreCase(profile)) {
                return;
            }
        }

        try {
            ensureUsersRoleColumn();
            ensureUsersPlanTypeColumn();
        } catch (Exception ex) {
            // Não derrubar a aplicação por falha de reparo; apenas logar.
            log.warn("DatabaseRepairRunner falhou: {}", ex.getMessage());
        }
    }

    private void ensureUsersRoleColumn() {
        // 1) cria coluna se não existir (nullable)
        jdbcTemplate.execute("""
                ALTER TABLE users
                ADD COLUMN IF NOT EXISTS role VARCHAR(32)
                """);
        // 2) backfill de nulos
        int updated = jdbcTemplate.update("""
                UPDATE users
                SET role = 'USER'
                WHERE role IS NULL
                """);
        if (updated > 0) {
            log.info("DatabaseRepairRunner: backfilled users.role for {} rows", updated);
        }
        // 3) tenta aplicar NOT NULL (se já existir e tiver nulos, isso não quebra)
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE users
                    ALTER COLUMN role SET NOT NULL
                    """);
        } catch (Exception ignored) {
            // Se houver dados incompatíveis, o Hibernate ainda pode trabalhar com a coluna nullable.
        }
    }

    private void ensureUsersPlanTypeColumn() {
        jdbcTemplate.execute("""
                ALTER TABLE users
                ADD COLUMN IF NOT EXISTS plan_type VARCHAR(32)
                """);
        int updated = jdbcTemplate.update("""
                UPDATE users
                SET plan_type = 'FREE'
                WHERE plan_type IS NULL
                """);
        if (updated > 0) {
            log.info("DatabaseRepairRunner: backfilled users.plan_type for {} rows", updated);
        }
        try {
            jdbcTemplate.execute("""
                    ALTER TABLE users
                    ALTER COLUMN plan_type SET NOT NULL
                    """);
        } catch (Exception ignored) {
        }
    }
}

