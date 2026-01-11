package com.locapro.backend.service.impl;

import com.locapro.backend.dto.auth.*;
import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.entity.EmailVerificationTokenEntity;
import com.locapro.backend.entity.UtilisateurEntity;
import com.locapro.backend.exception.*;
import com.locapro.backend.repository.EmailVerificationTokenRepository;
import com.locapro.backend.repository.UtilisateurRepository;
import com.locapro.backend.security.JwtService;
import com.locapro.backend.service.AuthService;
import com.locapro.backend.service.RefreshTokenService;
import com.locapro.backend.service.mail.MailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenRepository emailTokenRepository;
    private final MailSender mailSender;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(
            UtilisateurRepository utilisateurRepository,
            PasswordEncoder passwordEncoder,
            EmailVerificationTokenRepository emailTokenRepository,
            MailSender mailSender,
            JwtService jwtService,
            RefreshTokenService refreshTokenService
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailTokenRepository = emailTokenRepository;
        this.mailSender = mailSender;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Value("${app.auth.access-ttl-seconds:900}")
    private long accessTtlSeconds;
    @Value("${app.auth.email-verif-ttl-hours:24}")
    private long emailVerifTtlHours;


    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        final String email = normalizeEmail(request.getEmail());
        final String prenom = safeTrim(request.getPrenom());
        final String nom = safeTrim(request.getNom());
        final String numeroIpi = normalizeIpi(request.getNumeroIpi());

        if (utilisateurRepository.existsByEmail(email)) {
            throw new ConflictException("L'adresse e-mail est déjà utilisée");
        }

        if (numeroIpi != null && !numeroIpi.isBlank()
                && utilisateurRepository.existsByNumeroIpi(numeroIpi)) {
            throw new ConflictException("Le numéro IPI est déjà utilisé");
        }

        // === Vérification de la force du mot de passe ===
        final String rawPassword = request.getMotDePasse();
        validatePasswordStrength(rawPassword);

        final String passwordHash = passwordEncoder.encode(rawPassword);

        UtilisateurEntity user = new UtilisateurEntity();
        user.setPrenom(prenom);
        user.setNom(nom);
        user.setEmail(email);
        user.setNumeroIpi(numeroIpi);
        user.setDateNaissance(request.getDateNaissance());
        user.setPasswordHash(passwordHash);
        user.setEmailVerified(false);
        user.setEnabled(true);

        // adresse de domicile
        user.setRue(safeTrim(request.getRue()));
        user.setNumero(safeTrim(request.getNumero()));
        user.setBoite(safeTrim(request.getBoite()));
        user.setCodePostal(safeTrim(request.getCodePostal()));
        user.setVille(safeTrim(request.getVille()));

        String pays = safeTrim(request.getPays());
        if (pays == null || pays.isBlank()) {
            pays = "Belgique";
        }
        user.setPays(pays);

        user.setLatitude(request.getLatitude());
        user.setLongitude(request.getLongitude());

        user = utilisateurRepository.save(user);

        var token = new com.locapro.backend.entity.EmailVerificationTokenEntity();
        token.setUtilisateurId(user.getId());
        token.setToken(UUID.randomUUID());
        token.setExpiresAt(OffsetDateTime.now().plus(24, ChronoUnit.HOURS));
        token.setUsed(false);
        emailTokenRepository.save(token);

        mailSender.sendEmailVerification(email, token.getToken().toString());

        return toUserResponse(user, false);
    }



    @Override
    @Transactional
    public void verifyEmail(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Token manquant");
        }

        final UUID tokenUuid;
        try {
            tokenUuid = UUID.fromString(rawToken.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Token invalide");
        }

        var token = emailTokenRepository.findByToken(tokenUuid)
                .orElseThrow(() -> new NotFoundException("Token introuvable"));

        if (token.isUsed()) {
            throw new ConflictException("Token déjà utilisé");
        }

        if (token.getExpiresAt() == null || token.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new ConflictException("Token expiré");
        }

        var user = utilisateurRepository.findById(token.getUtilisateurId())
                .orElseThrow(() -> new NotFoundException("Utilisateur associé introuvable"));

        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            utilisateurRepository.save(user);
        }

        token.setUsed(true);
        emailTokenRepository.save(token);

        emailTokenRepository.deleteByUtilisateurId(user.getId());
    }

    @Override
    public LoginResult login(String email, String rawPassword) {
        // tu peux normaliser l’email ici si tu veux tolérer les majuscules/espaces
        final String normalizedEmail = normalizeEmail(email);

        var user = utilisateurRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new UnauthorizedException("Email ou mot de passe invalide"));

        if (!Boolean.TRUE.equals(user.isEmailVerified())) {
            throw new ForbiddenException("Email non vérifié");
        }

        if (!Boolean.TRUE.equals(user.isEnabled())) {
            throw new ForbiddenException("Compte désactivé");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new UnauthorizedException("Email ou mot de passe invalide");
        }

        var accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());
        var refresh = refreshTokenService.create(user);

        return new LoginResult(
                accessToken,
                accessTtlSeconds,
                refresh.getToken()
        );
    }

    @Override
    @Transactional
    public LoginResult refresh(String rawRefreshToken) {
        var rotated = refreshTokenService.rotate(rawRefreshToken);

        var user = rotated.getUser();
        if (!Boolean.TRUE.equals(user.isEmailVerified()) || !Boolean.TRUE.equals(user.isEnabled())) {
            throw new ForbiddenException("Compte inactif ou non vérifié");
        }

        var accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail());

        return new LoginResult(
                accessToken,
                accessTtlSeconds,
                rotated.getToken()
        );
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        // Idempotent : pas d’erreur si nul / inconnu
        refreshTokenService.revoke(refreshToken);
    }

    @Override
    @Transactional
    public void logoutAll(Long userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    @Override
    public void logoutByRefreshToken(String refreshToken) {
        refreshTokenService.revokeByToken(refreshToken);
    }

    @Override
    public void logoutAllDevices(Long userId) {
        refreshTokenService.revokeAllForUser(userId);
    }

    @Override
    @Transactional
    public ApiMessageResponse resendEmailVerification(String rawEmail) {
        if (rawEmail == null || rawEmail.isBlank()) {
            throw new BadRequestException("Adresse e-mail requise");
        }

        String email = rawEmail.trim().toLowerCase();

        UtilisateurEntity user = utilisateurRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Aucun compte associé à cette adresse e-mail."));

        // Si déjà vérifié → pas besoin de renvoyer un mail
        if (Boolean.TRUE.equals(user.isEmailVerified())) {
            return new ApiMessageResponse("Ce compte est déjà activé.");
        }

        // Nettoyer les anciens tokens pour cet utilisateur
        emailTokenRepository.deleteByUtilisateurId(user.getId());

        // Créer un nouveau token
        var tokenEntity = new EmailVerificationTokenEntity();
        tokenEntity.setUtilisateurId(user.getId());
        tokenEntity.setToken(UUID.randomUUID());
        tokenEntity.setExpiresAt(
                OffsetDateTime.now().plusHours(emailVerifTtlHours)
        );
        tokenEntity.setUsed(false);

        emailTokenRepository.save(tokenEntity);

        // Renvoyer le mail
        mailSender.sendEmailVerification(user.getEmail(), tokenEntity.getToken().toString());

        return new ApiMessageResponse("Un nouvel e-mail de confirmation vient de vous être envoyé.");
    }
    /* ======================== privates ======================== */

    private String normalizeEmail(String email) {
        if (email == null) throw new IllegalArgumentException("Email manquant");
        return email.trim().toLowerCase();
    }

    private String normalizeIpi(String ipi) {
        if (ipi == null) return null;
        String t = ipi.trim();
        return t.isEmpty() ? null : t;
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    private UserResponse toUserResponse(UtilisateurEntity e, boolean hasEntreprise) {
        UserResponse r = new UserResponse();
        r.setId(e.getId());
        r.setPrenom(e.getPrenom());
        r.setNom(e.getNom());
        r.setEmail(e.getEmail());
        r.setDateNaissance(e.getDateNaissance());
        r.setNumeroIpi(e.getNumeroIpi());
        r.setEmailVerified(e.isEmailVerified());
        r.setHasEntreprise(hasEntreprise);
        return r;
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
