package com.locapro.backend.controller;

import com.locapro.backend.dto.auth.*;
import com.locapro.backend.dto.common.ApiErrorResponse;
import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.dto.user.UserResponse;
import com.locapro.backend.service.AuthService;
import com.locapro.backend.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentification", description = "Endpoints pour l'inscription, la connexion et la gestion des tokens")
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

    // --- REGISTER ---
    @PostMapping("/register")
    @Operation(summary = "Inscription", description = "Crée un nouveau compte utilisateur.")
    @ApiResponse(responseCode = "200", description = "Compte créé avec succès")

    // Correction : Utilisation de ApiErrorResponse car géré par GlobalExceptionHandler
    @ApiResponse(responseCode = "400", description = "Données invalides (Validation)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))

    // Correction : Utilisation de ApiErrorResponse car géré par GlobalExceptionHandler
    @ApiResponse(responseCode = "409", description = "Email ou IPI déjà utilisé",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        UserResponse created = authService.register(request);
        return ResponseEntity.ok(created);
    }

    // --- VERIFY EMAIL ---
    @GetMapping("/verify-email")
    @Operation(summary = "Vérification Email", description = "Valide le compte utilisateur via le token reçu par email.")
    @ApiResponse(responseCode = "204", description = "Email vérifié avec succès")
    @ApiResponse(responseCode = "404", description = "Token introuvable ou utilisateur inconnu",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<Void> verifyEmail(@RequestParam("token") String token) {
        authService.verifyEmail(token);
        return ResponseEntity.noContent().build();
    }

    // --- LOGIN ---
    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifie l'utilisateur.")
    @ApiResponse(responseCode = "200", description = "Connexion réussie",
            content = @Content(schema = @Schema(implementation = AuthResponse.class)))

    // Correction : Utilisation de ApiErrorResponse
    @ApiResponse(responseCode = "401", description = "Identifiants incorrects (Email/Mot de passe)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))

    // Correction : Utilisation de ApiErrorResponse
    @ApiResponse(responseCode = "403", description = "Compte désactivé ou Email non vérifié",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest req) {

        LoginResult result = authService.login(req.email(), req.motDePasse());

        // Création du Cookie Refresh Token
        var cookie = ResponseCookie.from(refreshCookieName, result.refreshTokenValue())
                .httpOnly(true)
                .secure(false) // Mettre 'true' en PROD (nécessite HTTPS)
                .path("/auth/refresh")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(refreshTtlDays))
                .build();

        AuthResponse responseBody = new AuthResponse(
                result.accessToken(),
                result.accessExpiresInSeconds(),
                result.user()
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(responseBody);
    }

    // --- REFRESH ---
    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir Token", description = "Utilise le Cookie Refresh Token pour obtenir un nouvel Access Token.")
    @ApiResponse(responseCode = "200", description = "Nouveau token généré")
    @ApiResponse(responseCode = "401", description = "Refresh Token manquant ou invalide",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<RefreshTokenResponse> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshCookie
    ) {
        if (refreshCookie == null || refreshCookie.isBlank()) {
            return ResponseEntity.status(401).build();
        }

        var result = authService.refresh(refreshCookie);

        // On met à jour le cookie (rotation du refresh token pour sécurité)
        var cookie = ResponseCookie.from(refreshCookieName, result.refreshTokenValue())
                .httpOnly(true)
                .secure(false) // 'true' en PROD
                .path("/auth/refresh")
                .sameSite("Strict")
                .maxAge(Duration.ofDays(refreshTtlDays))
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new RefreshTokenResponse(
                        result.accessToken(),
                        result.accessExpiresInSeconds()
                ));
    }

    // --- LOGOUT (Simplifié) ---
    @PostMapping("/logout")
    @Operation(summary = "Déconnexion", description = "Invalide le Refresh Token en base et supprime le Cookie côté client.")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshCookie
    ) {
        // 1. Invalider en base si le cookie existe
        if (refreshCookie != null && !refreshCookie.isBlank()) {
            authService.logout(refreshCookie);
        }

        // 2. Nettoyer le cookie (Max-Age = 0)
        var clearCookie = ResponseCookie.from(refreshCookieName, "")
                .httpOnly(true)
                .secure(false) // 'true' en PROD
                .path("/auth/refresh")
                .maxAge(0) // Expire immédiatement
                .sameSite("Strict")
                .build();

        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
                .build();
    }

    // --- FORGOT PASSWORD ---
    @PostMapping("/forgot-password")
    @Operation(summary = "Mot de passe oublié", description = "Envoie un lien de réinitialisation si l'email existe.")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest req) {
        passwordResetService.requestReset(req);
        return ResponseEntity.noContent().build();
    }

    // --- RESET PASSWORD ---
    @PostMapping("/reset-password")
    @Operation(summary = "Réinitialiser mot de passe", description = "Change le mot de passe via le token reçu par email.")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest req) {
        passwordResetService.resetPassword(req);
        return ResponseEntity.noContent().build();
    }

    // --- RESEND EMAIL ---
    @PostMapping("/email-verification/resend")
    @Operation(summary = "Renvoyer Email Vérification", description = "Renvoie un email de confirmation si le compte n'est pas encore validé.")
    @ApiResponse(responseCode = "200", description = "Email renvoyé avec succès")
    @ApiResponse(responseCode = "400", description = "Email invalide ou manquant",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<ApiMessageResponse> resendEmailVerification(
            @RequestBody @Valid ResendEmailVerificationRequest request
    ) {
        var resp = authService.resendEmailVerification(request.email());
        return ResponseEntity.ok(resp);
    }
}