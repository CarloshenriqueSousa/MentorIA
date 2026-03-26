package com.mentoria.backend.controller;

import com.mentoria.backend.exception.ApiUnauthorizedException;
import com.mentoria.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Locale;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthHelper {

    private final UserRepository userRepository;

    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ApiUnauthorizedException("Não autenticado");
        }
        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserDetails userDetails)) {
            throw new ApiUnauthorizedException("Não autenticado");
        }

        String email = (userDetails.getUsername() == null ? "" : userDetails.getUsername())
                .toLowerCase(Locale.ROOT)
                .trim();
        if (email.isBlank()) {
            throw new ApiUnauthorizedException("Não autenticado");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiUnauthorizedException("Usuário autenticado não encontrado"))
                .getId();
    }
}