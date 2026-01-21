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

    // ❌ J'ai supprimé 'frontResetUrl' car c'est géré par SmtpMailSender maintenant

    public PasswordResetServiceImpl(UtilisateurRepository utilisateurRepository, PasswordResetTokenRepository tokenRepo, PasswordEncoder passwordEncoder, RefreshTokenService refreshTokenService, MailSender mailSender) {
        this.utilisateurRepository = utilisateurRepository;
        this.tokenRepo = tokenRepo;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.mailSender = mailSender;
    }

    @Override
    public void requestReset(ForgotPasswordRequest req) {
        String email = req.email().trim().toLowerCase();

        Optional<UtilisateurEntity> userOpt = utilisateurRepository.findByEmailIgnoreCase(email);

        userOpt.ifPresent(user -> {
            tokenRepo.deleteByUtilisateur_Id(user.getId());

            var t = new PasswordResetTokenEntity();
            t.setUtilisateur(user);
            t.setToken(UUID.randomUUID());
            t.setExpiresAt(OffsetDateTime.now().plusMinutes(resetTtlMinutes));
            t.setUsed(false);
            tokenRepo.save(t);

            // 👇 CORRECTION ICI : On envoie JUSTE le token (UUID)
            // Le SmtpMailSender se chargera d'ajouter "http://localhost:3000/reset-password?token="
            mailSender.sendPasswordReset(email, t.getToken().toString());
        });
    }

    @Override
    public ApiMessageResponse resetPassword(ResetPasswordRequest req) {
        UUID token;
        try {
            token = UUID.fromString(req.token().trim());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Token invalide");
        }

        var tokenEntity = tokenRepo.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new IllegalArgumentException("Token invalide ou déjà utilisé"));

        if (tokenEntity.getExpiresAt() == null || tokenEntity.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Token expiré");
        }

        var user = tokenEntity.getUtilisateur();

        final String rawPassword = req.newPassword();
        validatePasswordStrength(rawPassword); // Validation alignée avec le Register

        user.setPasswordHash(passwordEncoder.encode(rawPassword));

        tokenEntity.setUsed(true);

        refreshTokenService.revokeAllForUser(user.getId());

        return new ApiMessageResponse("Mot de passe modifié avec succès");
    }

    // 👇 VALIDATION MISE À JOUR (Identique à AuthServiceImpl)
    private void validatePasswordStrength(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire.");
        }

        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase); // Ajout
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase); // Ajout
        boolean longEnough = password.length() >= 8;

        // Même logique que AuthServiceImpl : Tout ce qui n'est pas lettre ou chiffre est spécial
        boolean hasSpecial = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));

        if (!longEnough || !hasDigit || !hasSpecial || !hasUpper || !hasLower) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial."
            );
        }
    }
}