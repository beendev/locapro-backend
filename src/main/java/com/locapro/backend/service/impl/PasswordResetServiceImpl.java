// com.locapro.backend.service.impl.PasswordResetServiceImpl
package com.locapro.backend.service.impl;

import com.locapro.backend.dto.auth.ForgotPasswordRequest;
import com.locapro.backend.dto.auth.ResetPasswordRequest;
import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.entity.PasswordResetTokenEntity;
import com.locapro.backend.entity.UtilisateurEntity;
import com.locapro.backend.repository.PasswordResetTokenRepository;
import com.locapro.backend.repository.UtilisateurRepository;
import com.locapro.backend.service.PasswordResetService;
import com.locapro.backend.service.RefreshTokenService;
import com.locapro.backend.service.mail.MailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final MailSender mailSender;

    @Value("${app.auth.pwreset-ttl-minutes:30}")
    private long resetTtlMinutes;

    // Lien front à insérer dans l’e-mail (le front lit ?token=.. et soumet ensuite au backend)
    @Value("${app.frontend.reset-password-url:https://app.locapro.example/reset-password}")
    private String frontResetUrl;

    public PasswordResetServiceImpl(UtilisateurRepository utilisateurRepository, PasswordResetTokenRepository tokenRepo, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService, MailSender mailSender) {
        this.utilisateurRepository = utilisateurRepository;
        this.tokenRepo = tokenRepo;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.mailSender = mailSender;
    }

    @Override
    public void requestReset(ForgotPasswordRequest req) {
        // Toujours normaliser l’email
        String email = req.email().trim().toLowerCase();

        // NE PAS révéler si le compte existe → on répondra 204 quoi qu’il arrive
        Optional<UtilisateurEntity> userOpt = utilisateurRepository.findByEmailIgnoreCase(email);

        userOpt.ifPresent(user -> {
            // (Optionnel) nettoyer anciens tokens
            tokenRepo.deleteByUtilisateur_Id(user.getId());

            // Créer un nouveau token
            var t = new PasswordResetTokenEntity();
            t.setUtilisateur(user);
            t.setToken(UUID.randomUUID());
            t.setExpiresAt(OffsetDateTime.now().plus(resetTtlMinutes, ChronoUnit.MINUTES));
            t.setUsed(false);
            tokenRepo.save(t);

            // Construire le lien pour l’e-mail : https://front/reset-password?token=XXXXX
            String link = frontResetUrl + "?token=" + t.getToken();

            // Envoyer l’e-mail
            mailSender.sendPasswordReset(email, link);
        });

        // Retour en controller : 204 No Content (même si user inconnu)
    }

    @Override
    public ApiMessageResponse resetPassword(ResetPasswordRequest req) {
        UUID token;
        try {
            token = UUID.fromString(req.token().trim());
        } catch (IllegalArgumentException e) {
            // On ne précise pas trop : on lève une 400 ou 401 en controller
            throw new IllegalArgumentException("Token invalide");
        }

        var tokenEntity = tokenRepo.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide ou déjà utilisé"));

        if (tokenEntity.getExpiresAt() == null || tokenEntity.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Token expiré");
        }

        var user = tokenEntity.getUtilisateur();
        // Mettre à jour le mot de passe
        final String rawPassword = req.newPassword();
        validatePasswordStrength(rawPassword);

        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        // Marquer le token comme utilisé
        tokenEntity.setUsed(true);

        // Révoquer tous les refresh → déconnecte toutes les sessions
        refreshTokenService.revokeAllForUser(user.getId());

        return new ApiMessageResponse("Mot de passe changer avec success");
        // Flush via transaction @Transactional
    }

    private void validatePasswordStrength(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire.");
        }

        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecial = password.chars().anyMatch(ch ->
                "!@#$%^&*()_+-=[]{}|;':\",.<>/?`~\\".indexOf(ch) >= 0
        );
        boolean longEnough = password.length() >= 8;

        if (!longEnough || !hasDigit || !hasSpecial) {
            // Remplace IllegalArgumentException par ta propre exception HTTP 400 si tu en as une
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir au moins 8 caractères, " +
                            "au moins un chiffre et au moins un caractère spécial."
            );
        }
    }
}
