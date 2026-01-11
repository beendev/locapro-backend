// com.locapro.backend.security.RestAuthHandlers.java
package com.locapro.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class RestAuthHandlers {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static final AuthenticationEntryPoint UNAUTHORIZED_JSON = (req, res, ex) -> {
        // 401 → pas authentifié (token manquant/invalid)
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        var body = Map.of(
                "error", "unauthorized",
                "message", messageForUnauthorized(req, ex),
                "path", req.getRequestURI()
        );
        MAPPER.writeValue(res.getWriter(), body);
    };

    public static final AccessDeniedHandler FORBIDDEN_JSON = (req, res, ex) -> {
        // 403 → authentifié mais pas autorisé
        res.setStatus(HttpServletResponse.SC_FORBIDDEN);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        res.setCharacterEncoding(StandardCharsets.UTF_8.name());
        var body = Map.of(
                "error", "forbidden",
                "message", "Accès refusé.",
                "path", req.getRequestURI()
        );
        MAPPER.writeValue(res.getWriter(), body);
    };

    private static String messageForUnauthorized(HttpServletRequest req, Exception ex) {
        // Tu peux raffiner si besoin (ex: distinguer “manquant” vs “invalide”)
        return "Non authentifié : Bearer token manquant ou invalide.";
    }
}
