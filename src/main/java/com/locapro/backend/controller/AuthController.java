package com.locapro.backend.controller;

import com.locapro.backend.dto.auth.*;
import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.security.JwtAuthFilter.UserPrincipal;
import com.locapro.backend.service.AuthService;
import com.locapro.backend.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Value("${app.auth.refresh-cookie-name:refresh_token}")
    private String refreshCookieName;

    @Value("${app.auth.refresh-ttl-days:30}")
    private long refreshTtlDays;

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    public AuthController(AuthService authService, PasswordResetService passwordResetService) {
        this.authService = authService;
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse created = authService.register(request);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.noContent().build(); // 204
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest req) {

        // 1. Le Service renvoie TOUT (LoginResult contient le refresh token)
        LoginResult result = authService.login(req.getEmail(), req.getMotDePasse());

        // 2. Le Controller utilise le refresh token pour créer le COOKIE

        var cookie = ResponseCookie.from(refreshCookieName, result.refreshTokenValue())
                .httpOnly(true)
                .secure(false) // true en prod
                .path("/auth/refresh")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(refreshTtlDays))
                .build();

        // 3. Le Controller crée la réponse ÉPURÉE pour le JSON (AuthResponse)
        AuthResponse responseBody = new AuthResponse(
                result.accessToken(),
                result.accessExpiresInSeconds()
        );

        // 4. On envoie : Cookie (caché) + AuthResponse (visible)
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseBody);
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshCookie
    ) {
        if (refreshCookie == null || refreshCookie.isBlank()) {
            // Pas de cookie = pas de refresh possible
            // Tu peux aussi renvoyer 401 avec ton format d'erreur custom
            return ResponseEntity.status(401).build();
        }

        // 1) On demande au service d'échanger ce refresh token contre :
        //    - un nouvel access token
        //    - un nouveau refresh token
        var result = authService.refresh(refreshCookie);

        // 2) On remet à jour le cookie avec le "nouveau" refresh token
        var cookie = ResponseCookie.from(refreshCookieName, result.refreshTokenValue())
                .httpOnly(true)
                .secure(false)             // true en prod
                .path("/auth/refresh")
                .sameSite("Lax")
                .maxAge(Duration.ofDays(refreshTtlDays))
                .build();

        // 3) On renvoie l'access token au front
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new RefreshTokenResponse(
                        result.accessToken(),
                        result.accessExpiresInSeconds()
                ));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshCookie,
            @RequestParam(name = "allDevices", defaultValue = "false") boolean allDevices,
            Authentication authentication
    ) {
        // Révocation en base de données
        if (!allDevices && refreshCookie != null) {
            authService.logoutByRefreshToken(refreshCookie);
        }
        if (allDevices && authentication != null) {
            var p = (UserPrincipal) authentication.getPrincipal();
            authService.logoutAllDevices(p.id());
        }

        // NETTOYAGE DU COOKIE (Important !)
        var clearCookie = ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(false) // true en prod
                .path("/auth/refresh")
                .maxAge(0) // Expire immédiatement
                .sameSite("Strict")
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .build();
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(Authentication authentication) {
        UserPrincipal p = (UserPrincipal) authentication.getPrincipal();
        return ResponseEntity.ok(Map.of(
                "userId", p.id(),
                "email", p.email()
        ));
    }





    @PostMapping("/logout-all")
    public ResponseEntity<Void> logoutAll(Authentication authentication) {
        var p = (UserPrincipal) authentication.getPrincipal();
        authService.logoutAll(p.id());

        var clear = ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(false) // true en prod
                .path("/auth/refresh")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clear.toString())
                .build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest req) {
        passwordResetService.requestReset(req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest req) {
        passwordResetService.resetPassword(req);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/email-verification/resend")
    public ResponseEntity<ApiMessageResponse> resendEmailVerification(
            @RequestBody ResendEmailVerificationRequest request
    ) {
        var resp = authService.resendEmailVerification(request.email());
        return ResponseEntity.ok(resp);
    }

}
