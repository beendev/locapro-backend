package com.locapro.backend.service.impl;

import com.locapro.backend.dto.agence.AgenceRequest;
import com.locapro.backend.dto.agence.AgenceResponse;
import com.locapro.backend.entity.AgenceEntity;
import com.locapro.backend.entity.UtilisateurAgenceEntity;
import com.locapro.backend.exception.BadRequestException;
import com.locapro.backend.exception.ConflictException;
import com.locapro.backend.exception.NotFoundException;
import com.locapro.backend.repository.*;
import com.locapro.backend.service.AgenceService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AgenceServiceImpl implements AgenceService {

    private final AgenceRepository agenceRepo;
    private final UtilisateurAgenceRepository userAgenceRepo;
    private final UtilisateurRepository userRepo;
    private final EntrepriseRepository entrepriseRepo;

    public AgenceServiceImpl(
            AgenceRepository agenceRepo,
            UtilisateurAgenceRepository userAgenceRepo,
            UtilisateurRepository userRepo,
            EntrepriseRepository entrepriseRepo
    ) {
        this.agenceRepo = agenceRepo;
        this.userAgenceRepo = userAgenceRepo;
        this.userRepo = userRepo;
        this.entrepriseRepo = entrepriseRepo;
    }

    @Override
    public AgenceResponse create(Long userId, AgenceRequest req) {
        if (userId == null) throw new BadRequestException("Utilisateur non authentifié");
        var user = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        // Vérif entreprise existante
        var entreprise = entrepriseRepo.findByProprietaire_IdAndEnabledTrue(userId)
                .orElseThrow(() -> new NotFoundException("Aucune entreprise active pour cet utilisateur"));

        // Vérif qu’il n’existe pas déjà une agence pour cette entreprise
        agenceRepo.findByEntreprise_IdAndEnabledTrue(entreprise.getId())
                .ifPresent(a -> { throw new ConflictException("Une agence existe déjà pour cette entreprise"); });

        // Vérif que l’utilisateur n’est pas déjà dans une autre agence
        if (userAgenceRepo.existsByUtilisateur_Id(userId)) {
            throw new ConflictException("L’utilisateur appartient déjà à une agence");
        }

        // Création de l’agence
        var agence = new AgenceEntity();
        agence.setNom(req.nom().trim());
        agence.setAdminAgence(user);
        agence.setEntreprise(entreprise);
        agence.setEnabled(true);
        var saved = agenceRepo.save(agence);

        // Ajout du lien utilisateur ↔ agence
        var userAgence = new UtilisateurAgenceEntity();
        userAgence.setUtilisateur(user);
        userAgence.setAgence(saved);
        userAgence.setRoleDansAgence("ADMIN_AGENCE");
        userAgenceRepo.save(userAgence);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AgenceResponse getCurrent(Long userId) {
        var link = userAgenceRepo.findByUtilisateur_Id(userId)
                .orElseThrow(() -> new NotFoundException("Aucune agence trouvée pour cet utilisateur"));
        return toResponse(link.getAgence());
    }

    private AgenceResponse toResponse(AgenceEntity e) {
        return new AgenceResponse(
                e.getId(),
                e.getNom(),
                e.getEntreprise() != null ? e.getEntreprise().getId() : null,
                e.getAdminAgence() != null ? e.getAdminAgence().getId() : null,
                e.getEnabled()
        );
    }
}
