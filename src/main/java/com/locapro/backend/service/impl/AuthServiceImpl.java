package com.locapro.backend.service.impl;

import com.locapro.backend.dto.auth.*;
import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.dto.user.UserResponse;
import com.locapro.backend.entity.EmailVerificationTokenEntity;
import com.locapro.backend.entity.UtilisateurEntity;
import com.locapro.backend.exception.*;
import com.locapro.backend.mapper.UserMapper;
import com.locapro.backend.repository.EmailVerificationTokenRepository;
import com.locapro.backend.repository.EntrepriseRepository; // 👈 Import ajouté
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
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UtilisateurRepository utilisateurRepository;
    private final EntrepriseRepository entrepriseRepository; // 👈 Ajouté
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationTokenRepository emailTokenRepository;
    private final MailSender mailSender;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserMapper userMapper;

    public AuthServiceImpl(
            UtilisateurRepository utilisateurRepository,
            EntrepriseRepository entrepriseRepository, // 👈 Ajouté au constructeur
            PasswordEncoder passwordEncoder,
            EmailVerificationTokenRepository emailTokenRepository,
            MailSender mailSender,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            UserMapper userMapper
    ) {
        this.utilisateurRepository = utilisateurRepository;
        this.entrepriseRepository = entrepriseRepository; // 👈 Assignation
        this.passwordEncoder = passwordEncoder;
        this.emailTokenRepository = emailTokenRepository;
        this.mailSender = mailSender;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userMapper = userMapper;
    }

    @Value("${app.auth.access-ttl-seconds:900}")
    private long accessTtlSeconds;
    @Value("${app.auth.email-verif-ttl-hours:24}")
    private long emailVerifTtlHours;

    @Override
    @Transactional
    public UserResponse register(RegisterUserRequest request) {
        final String email = normalizeEmail(request.email());
        final String prenom = safeTrim(request.prenom());
        final String nom = safeTrim(request.nom());
        final String numeroIpi = normalizeIpi(request.numeroIpi());

        if (utilisateurRepository.existsByEmail(email)) {
            throw new ConflictException("L'adresse e-mail est déjà utilisée");
        }

        if (numeroIpi != null && !numeroIpi.isBlank()
                && utilisateurRepository.existsByNumeroIpi(numeroIpi)) {
            throw new ConflictException("Le numéro IPI est déjà utilisé");
        }

        // === Vérification de la force du mot de passe ===
        final String rawPassword = request.motDePasse();
        validatePasswordStrength(rawPassword);

        final String passwordHash = passwordEncoder.encode(rawPassword);

        UtilisateurEntity user = new UtilisateurEntity();
        user.setPrenom(prenom);
        user.setNom(nom);
        user.setEmail(email);
        user.setNumeroIpi(numeroIpi);
        user.setDateNaissance(request.dateNaissance());
        user.setPasswordHash(passwordHash);
        user.setTelephone(request.telephone());
        user.setEmailVerified(false);
        user.setEnabled(true);

        // Adresse de domicile
        user.setRue(safeTrim(request.rue()));
        user.setNumero(safeTrim(request.numero()));
        user.setBoite(safeTrim(request.boite()));
        user.setCodePostal(safeTrim(request.codePostal()));
        user.setVille(safeTrim(request.ville()));
        user.setCommune(safeTrim(request.commune()));

        String pays = safeTrim(request.pays());
        if (pays == null || pays.isBlank()) {
            pays = "Belgique";
        }
        user.setPays(pays);

        user.setLatitude(request.latitude());
        user.setLongitude(request.longitude());

        user = utilisateurRepository.save(user);

        var token = new com.locapro.backend.entity.EmailVerificationTokenEntity();
        token.setUtilisateurId(user.getId());
        token.setToken(UUID.randomUUID());
        token.setExpiresAt(OffsetDateTime.now().plusHours(24));
        token.setUsed(false);
        emailTokenRepository.save(token);

        mailSender.sendEmailVerification(email, token.getToken().toString());

        // Ici, on utilise le mapper simple (pas d'entreprise au register)
        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public void verifyEmail(String rawToken) {
        // 1. Validation basique du string
        if (rawToken == null || rawToken.isBlank()) {
            throw new IllegalArgumentException("Token manquant");
        }

        final UUID tokenUuid;
        try {
            tokenUuid = UUID.fromString(rawToken.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Token invalide");
        }

        // 2. On cherche le token
        // S'il n'existe pas, c'est peut-être qu'il a déjà été validé/supprimé 1ms avant.
        var tokenOpt = emailTokenRepository.findByToken(tokenUuid);

        if (tokenOpt.isEmpty()) {
            // Ici, on peut considérer que c'est une erreur (lien mort)
            // OU considérer que c'est un succès si l'user est déjà actif.
            // Pour être strict :
            throw new NotFoundException("Lien invalide ou déjà utilisé.");
        }

        var token = tokenOpt.get();

        // 3. On récupère l'utilisateur associé au token
        var user = utilisateurRepository.findById(token.getUtilisateurId())
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        // 4. On active l'utilisateur (SI ce n'est pas déjà fait)
        if (!Boolean.TRUE.equals(user.isEmailVerified())) {
            user.setEmailVerified(true);
            utilisateurRepository.save(user); // ✅ On sauvegarde l'utilisateur (il devient actif)
        }

        // 5. ON SUPPRIME LES TOKENS (Le ménage) 🧹
        // Cette ligne supprime UNIQUEMENT dans la table 'email_verification_tokens'
        // Grâce au @Modifying/@Query, ça ne plante pas si c'est déjà vide.
        emailTokenRepository.deleteAllTokensByUser(user.getId());
    }


    @Override
    public LoginResult login(String email, String rawPassword) {
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

        // 👇 LOGIQUE ONBOARDING : Récupérer l'entreprise ID si elle existe
        Long entrepriseId = entrepriseRepository.findIdByUserIdAndEnabledTrue(user.getId())
                .orElse(null);

        // Convertir en DTO complet pour le front
        UserResponse userDto = userMapper.toUserResponseWithEntreprise(user, entrepriseId);

        return new LoginResult(
                accessToken,
                accessTtlSeconds,
                refresh.getToken(),
                userDto // 👈 Ajouté à la réponse
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

        // Même logique pour le refresh : on renvoie le user à jour
        Long entrepriseId = entrepriseRepository.findIdByUserIdAndEnabledTrue(user.getId())
                .orElse(null);

        UserResponse userDto = userMapper.toUserResponseWithEntreprise(user, entrepriseId);

        return new LoginResult(
                accessToken,
                accessTtlSeconds,
                rotated.getToken(),
                userDto // 👈 Ajouté ici aussi
        );
    }

    @Override
    public void logout(String refreshToken) {
        refreshTokenService.revokeByToken(refreshToken);
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

        if (Boolean.TRUE.equals(user.isEmailVerified())) {
            return new ApiMessageResponse("Ce compte est déjà activé.");
        }

        emailTokenRepository.deleteByUtilisateurId(user.getId());

        var tokenEntity = new EmailVerificationTokenEntity();
        tokenEntity.setUtilisateurId(user.getId());
        tokenEntity.setToken(UUID.randomUUID());
        tokenEntity.setExpiresAt(OffsetDateTime.now().plusHours(emailVerifTtlHours));
        tokenEntity.setUsed(false);

        emailTokenRepository.save(tokenEntity);

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

    private void validatePasswordStrength(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Le mot de passe est obligatoire.");
        }

        // 1. Les vérifications de base
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        boolean hasUpper = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLower = password.chars().anyMatch(Character::isLowerCase);
        boolean longEnough = password.length() >= 8;

        // 2. 👇 CORRECTION : Tout caractère qui n'est NI lettre NI chiffre est considéré comme spécial
        // Cela correspond à la regex frontend /[^A-Za-z0-9]/
        boolean hasSpecial = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));

        // 3. Validation finale
        if (!longEnough || !hasDigit || !hasSpecial || !hasUpper || !hasLower) {
            throw new IllegalArgumentException(
                    "Le mot de passe doit contenir 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial."
            );
        }
    }



}