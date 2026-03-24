package com.mentoria.backend.config.supabase;

import com.mentoria.backend.config.SeedTestUserProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({SupabaseProperties.class, SeedTestUserProperties.class})
public class SupabaseConfig {
}
