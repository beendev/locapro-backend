package com.locapro.backend.service.impl;

import com.locapro.backend.dto.user.UserResponse;
import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.dto.user.ChangePasswordRequest;
import com.locapro.backend.dto.user.UpdateUserProfileRequest;
import com.locapro.backend.entity.UtilisateurEntity;
import com.locapro.backend.exception.NotFoundException;
import com.locapro.backend.exception.BadRequestException;
import com.locapro.backend.mapper.UserMapper;
import com.locapro.backend.repository.EntrepriseRepository;
import com.locapro.backend.repository.UtilisateurRepository;
import com.locapro.backend.service.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import  java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UtilisateurRepository utilisateurRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EntrepriseRepository entrepriseRepository;

    public UserServiceImpl(UtilisateurRepository utilisateurRepository, PasswordEncoder passwordEncoder, UserMapper userMapper, EntrepriseRepository entrepriseRepository) {
        this.utilisateurRepository = utilisateurRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
        this.entrepriseRepository = entrepriseRepository;
    }

    @Override
    public UserResponse getCurrentUser(Long userId) {
        UtilisateurEntity user = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        // 👇 ON DOIT CHERCHER L'ENTREPRISE ICI AUSSI
        Long entrepriseId = entrepriseRepository.findIdByUserIdAndEnabledTrue(user.getId())
                .orElse(null);

        // 👇 ET UTILISER LE MAPPER SPÉCIAL
        return userMapper.toUserResponseWithEntreprise(user, entrepriseId);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateUserProfileRequest request) {
        UtilisateurEntity user = findById(userId);

        // === 1. Infos Personnelles ===
        // On utilise safeTrim pour éviter les espaces vides inutiles
        if (request.prenom() != null) user.setPrenom(safeTrim(request.prenom()));
        if (request.nom() != null) user.setNom(safeTrim(request.nom()));
        if (request.telephone() != null) user.setTelephone(safeTrim(request.telephone()));

        // === 2. Adresse de domicile ===
        if (request.rue() != null) user.setRue(safeTrim(request.rue()));
        if (request.numero() != null) user.setNumero(safeTrim(request.numero()));
        if (request.boite() != null) user.setBoite(safeTrim(request.boite()));
        if (request.codePostal() != null) user.setCodePostal(safeTrim(request.codePostal()));
        if (request.ville() != null) user.setVille(safeTrim(request.ville()));
        if (request.pays() != null) user.setPays(safeTrim(request.pays()));



        // Les Doubles n'ont pas besoin de trim
        if (request.latitude() != null) user.setLatitude(request.latitude());
        if (request.longitude() != null) user.setLongitude(request.longitude());


        UtilisateurEntity savedUser = utilisateurRepository.save(user);


        return userMapper.toUserResponse(savedUser);
    }



    @Override
    @Transactional
    public ApiMessageResponse changePassword(Long userId, ChangePasswordRequest request) {
        UtilisateurEntity user = findById(userId);

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(request.oldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("L'ancien mot de passe est incorrect.");
        }


        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        utilisateurRepository.save(user);
        return new ApiMessageResponse("changement de mot de passe réussi !");
    }

    @Override
    @Transactional
    public ApiMessageResponse anonymiserUser(Long userId) {
        UtilisateurEntity user = findById(userId);

        user.setNom(UUID.randomUUID().toString());
        user.setPrenom(UUID.randomUUID().toString());
        user.setPasswordHash(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setTelephone("000000000");
        user.setEmail(UUID.randomUUID() +"@anonymiser.be");
        user.setRue(null);
        user.setNumero(null);
        user.setBoite(null);
        user.setCodePostal(null);
        user.setVille(null);
        user.setPays(null);
        user.setLatitude(null);
        user.setLongitude(null);
        user.setNumeroIpi(null);
        user.setDateNaissance(LocalDate.now());
        user.setEnabled(false);
        utilisateurRepository.save(user);

        return new ApiMessageResponse("votre compte à été anonymiser !");
    }

    // --- Helpers privés ---

    private UtilisateurEntity findById(Long id) {
        return utilisateurRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur non trouvé"));
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    // Tu devras probablement dupliquer ou partager cette méthode de mapping

}