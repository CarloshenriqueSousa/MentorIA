package com.mentoria.backend.security;

import com.mentoria.backend.model.User;
import com.mentoria.backend.model.UserRole;
import com.mentoria.backend.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /** Hash estável para usuários só-Supabase (login não passa por senha local). */
    private String supabaseOnlyPasswordPlaceholder;

    @PostConstruct
    void initPlaceholder() {
        supabaseOnlyPasswordPlaceholder = passwordEncoder.encode("__supabase_auth_only__");
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + email));

        String passwordForSpring = user.getPasswordHash();
        if (passwordForSpring == null || passwordForSpring.isBlank()) {
            passwordForSpring = supabaseOnlyPasswordPlaceholder;
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        if (user.getRole() != null && user.getRole() == UserRole.ADMIN) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getPlanType().name()));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(passwordForSpring)
                .authorities(authorities)
                .accountLocked(!user.isActive())
                .disabled(!user.isActive())
                .build();
    }
}