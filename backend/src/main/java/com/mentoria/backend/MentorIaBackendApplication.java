package com.mentoria.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
public class MentorIaBackendApplication {

	public static void main(String[] args) {
		loadDotEnv();
		SpringApplication.run(MentorIaBackendApplication.class, args);
	}

	/**
	 * Carrega `.env` da raiz do repositório ou do diretório atual antes do Spring resolver placeholders.
	 * Spring Boot não lê `.env` nativamente (diferente do Nuxt).
	 */
	private static void loadDotEnv() {
		List<Path> candidates = List.of(
				Path.of(".env"),
				Path.of("..", ".env"),
				Path.of("..", "..", ".env")
		);

		for (Path path : candidates) {
			if (!Files.isRegularFile(path)) {
				continue;
			}
			try {
				for (String rawLine : Files.readAllLines(path)) {
					String line = rawLine.trim();
					if (line.isEmpty() || line.startsWith("#")) {
						continue;
					}
					int eq = line.indexOf('=');
					if (eq <= 0) {
						continue;
					}
					String key = line.substring(0, eq).trim();
					String value = line.substring(eq + 1).trim();
					if ((value.startsWith("\"") && value.endsWith("\""))
							|| (value.startsWith("'") && value.endsWith("'"))) {
						value = value.substring(1, value.length() - 1);
					}
					if (System.getenv(key) == null && System.getProperty(key) == null) {
						System.setProperty(key, value);
					}
				}
				return;
			} catch (IOException ignored) {
				// tenta próximo caminho
			}
		}
	}
}
