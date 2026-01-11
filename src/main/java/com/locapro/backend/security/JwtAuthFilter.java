package com.locapro.backend.security;

import com.locapro.backend.entity.UtilisateurEntity;
import com.locapro.backend.repository.UtilisateurRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Valide le JWT “maison” et peuple le SecurityContext. */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UtilisateurRepository utilisateurRepository;

    public JwtAuthFilter(JwtService jwtService, UtilisateurRepository utilisateurRepository) {
        this.jwtService = jwtService;
        this.utilisateurRepository = utilisateurRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return; // pas de token → on laisse passer, ça tombera sur 401 si endpoint protégé
        }

        String token = auth.substring(7).trim();
        try {
            Claims claims = jwtService.parseAndValidate(token);
            Long userId = claims.get("uid", Number.class).longValue(); // mis à l’émission
            String email = claims.getSubject();

            // Option V1 : on recharge l’utilisateur (pour vérifier enabled, etc.)
            Optional<UtilisateurEntity> opt = utilisateurRepository.findById(userId);
            if (opt.isEmpty() || Boolean.FALSE.equals(opt.get().isEnabled())) {
                filterChain.doFilter(request, response);
                return;
            }

            // Pas de rôles pour l’instant
            List<GrantedAuthority> authorities = Collections.emptyList();

            // On met un principal léger (id + email) dans le SecurityContext
            UserPrincipal principal = new UserPrincipal(userId, email);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (Exception e) {
            // Token invalide/expiré → on nettoie et on laisse la chaîne (finira en 401)
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    /** Principal léger accessible dans les contrôleurs. */
    public record UserPrincipal(Long id, String email) {}
}
