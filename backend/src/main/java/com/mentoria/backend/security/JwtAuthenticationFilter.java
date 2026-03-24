package com.mentoria.backend.security;

import com.mentoria.backend.service.SupabaseUserProvisioningService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final SupabaseJwtService supabaseJwtService;
    private final SupabaseUserProvisioningService supabaseUserProvisioningService;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String token = extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (tryAuthenticateWithSupabase(token, request)) {
                filterChain.doFilter(request, response);
                return;
            }

            if (tryAuthenticateWithLegacyJwt(token, request)) {
                filterChain.doFilter(request, response);
                return;
            }
        } catch (Exception e) {
            log.warn("JWT validation failed: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private boolean tryAuthenticateWithSupabase(String token, HttpServletRequest request) {
        Optional<Claims> claimsOpt = supabaseJwtService.tryParseValidClaims(token);
        if (claimsOpt.isEmpty()) {
            return false;
        }
        Claims claims = claimsOpt.get();
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return true;
        }

        supabaseUserProvisioningService.ensureLocalUserFromSupabaseClaims(claims);
        String email = claims.get("email", String.class);
        if (email == null || email.isBlank()) {
            return false;
        }
        email = email.toLowerCase().trim();

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authToken);
        return true;
    }

    private boolean tryAuthenticateWithLegacyJwt(String token, HttpServletRequest request) {
        try {
            final String userEmail = jwtTokenProvider.extractUsername(token);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = userDetailsService.loadUserByUsername(userEmail);

                if (jwtTokenProvider.isTokenValid(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    return true;
                }
            }
        } catch (Exception ignored) {
            // token não é JWT legado válido
        }
        return false;
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
