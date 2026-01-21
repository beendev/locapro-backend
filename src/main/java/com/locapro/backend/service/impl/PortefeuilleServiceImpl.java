package com.locapro.backend.service.impl;

import com.locapro.backend.dto.bien.BienResponse;
import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.dto.portefeuille.PortefeuilleAjouterMembreRequest;
import com.locapro.backend.dto.portefeuille.PortefeuilleCreateRequest;
import com.locapro.backend.dto.portefeuille.PortefeuilleMembreResponse;
import com.locapro.backend.dto.portefeuille.PortefeuilleResponse;
import com.locapro.backend.entity.*;
import com.locapro.backend.exception.ForbiddenException;
import com.locapro.backend.exception.NotFoundException;
import com.locapro.backend.mapper.BienResponseMapper;
import com.locapro.backend.mapper.PortefeuilleMapper;
import com.locapro.backend.mapper.PortefeuilleMembreMapper;
import com.locapro.backend.repository.PortefeuilleMembreRepository;
import com.locapro.backend.repository.PortefeuilleRepository;
import com.locapro.backend.repository.UtilisateurAgenceRepository;
import com.locapro.backend.repository.UtilisateurRepository;
import com.locapro.backend.service.PortefeuilleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.locapro.backend.repository.BienRepository;
import com.locapro.backend.exception.ConflictException;
import java.util.Objects;


import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class PortefeuilleServiceImpl implements PortefeuilleService {

    private final UtilisateurAgenceRepository utilisateurAgenceRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final PortefeuilleRepository portefeuilleRepository;
    private final PortefeuilleMembreRepository portefeuilleMembreRepository;
    private final PortefeuilleMapper portefeuilleMapper;
    private final PortefeuilleMembreMapper portefeuilleMembreMapper;
    private final BienRepository bienRepository;
    private final BienResponseMapper bienResponseMapper;

    public PortefeuilleServiceImpl(
            UtilisateurAgenceRepository utilisateurAgenceRepository,
            UtilisateurRepository utilisateurRepository,
            PortefeuilleRepository portefeuilleRepository,
            PortefeuilleMembreRepository portefeuilleMembreRepository,
            PortefeuilleMapper portefeuilleMapper,
            PortefeuilleMembreMapper portefeuilleMembreMapper,
            BienRepository bienRepository, BienResponseMapper bienResponseMapper
    ) {
        this.utilisateurAgenceRepository = utilisateurAgenceRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.portefeuilleRepository = portefeuilleRepository;
        this.portefeuilleMembreRepository = portefeuilleMembreRepository;
        this.portefeuilleMapper = portefeuilleMapper;
        this.portefeuilleMembreMapper = portefeuilleMembreMapper;
        this.bienRepository = bienRepository;
        this.bienResponseMapper = bienResponseMapper;
    }

    @Override
    public PortefeuilleResponse creerPortefeuilleAgence(Long currentUserId,
                                                        PortefeuilleCreateRequest request) {

        // 1) Lien user ↔ agence
        UtilisateurAgenceEntity lien = utilisateurAgenceRepository
                .findFirstByUtilisateurIdAndEnabledTrue(currentUserId)
                .orElseThrow(() ->
                        new ForbiddenException("Vous n'êtes rattaché à aucune agence active.")
                );

        if (!aDroitAdminAgence(lien)) {
            throw new ForbiddenException("Vous n'avez pas les droits pour créer un portefeuille dans cette agence.");
        }

        AgenceEntity agence = lien.getAgence();
        if (agence == null) {
            throw new ForbiddenException("Agence introuvable pour cet utilisateur.");
        }

        UtilisateurEntity user = utilisateurRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable"));

        OffsetDateTime now = OffsetDateTime.now();

        // 2) Créer le portefeuille via le mapper
        PortefeuilleEntity p = portefeuilleMapper.fromAgenceCreateRequest(
                request,
                agence,
                user,
                now
        );
        p = portefeuilleRepository.save(p);

        // 3) Ajouter le créateur comme membre gestionnaire via le mapper
        PortefeuilleMembreEntity membre =
                portefeuilleMembreMapper.asGestionnaire(p, user, now);

        portefeuilleMembreRepository.save(membre);

        // 4) DTO de réponse
        return portefeuilleMapper.toResponse(p);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortefeuilleResponse> listerPortefeuillesAgence(Long currentUserId) {
        // Tous les portefeuilles où je suis membre actif
        var membres = portefeuilleMembreRepository
                .findByUtilisateurIdAndEnabledTrue(currentUserId);

        return membres.stream()
                .map(PortefeuilleMembreEntity::getPortefeuille)
                .filter(PortefeuilleEntity::isEnabled)
                .map(portefeuilleMapper::toResponse)
                .toList();
    }

    @Override
    public ApiMessageResponse quitterPortefeuille(Long portefeuilleId, Long currentUserId) {
        var membreOpt = portefeuilleMembreRepository
                .findByPortefeuilleIdAndUtilisateurIdAndEnabledTrue(portefeuilleId, currentUserId);

        if (membreOpt.isEmpty()) {
            // Idempotent : si tu n'es pas membre, on ne crie pas
            return new ApiMessageResponse("Vous n'êtes pas membre de ce portefeuille.");
        }

        var membre = membreOpt.get();

        // (Optionnel) empêcher de quitter si tu es le dernier GESTIONNAIRE
        // à voir plus tard si tu veux ce type de règle métier

        membre.setEnabled(false);
        portefeuilleMembreRepository.save(membre);

        return new ApiMessageResponse("Vous avez quitté ce portefeuille.");
    }

    @Override
    public ApiMessageResponse ajouterBienAuPortefeuille(Long portefeuilleId,
                                                        Long bienId,
                                                        Long currentUserId) {

        // 1) Charger le portefeuille
        PortefeuilleEntity portefeuille = portefeuilleRepository.findById(portefeuilleId)
                .orElseThrow(() -> new NotFoundException("Portefeuille non trouvé."));

        if (!portefeuille.isEnabled()) {
            throw new ForbiddenException("Ce portefeuille est désactivé.");
        }

        // 2) Vérifier que l'utilisateur courant est membre actif du portefeuille
        var membreCourant = portefeuilleMembreRepository
                .findByPortefeuilleIdAndUtilisateurIdAndEnabledTrue(portefeuilleId, currentUserId)
                .orElseThrow(() ->
                        new ForbiddenException("Vous n'êtes pas membre de ce portefeuille.")
                );

        // 3) Vérifier qu'il a le rôle “admin du portefeuille”
        if (!aDroitAdminPortefeuille(membreCourant)) {
            throw new ForbiddenException("Vous n'avez pas les droits pour modifier ce portefeuille.");
        }

        // 4) Charger le bien
        BienEntity bien = bienRepository.findById(bienId)
                .orElseThrow(() -> new NotFoundException("Bien non trouvé : " + bienId));

        // 5) Cohérence d'agence : un portefeuille d'agence ne peut gérer que des biens de cette agence
        if ("AGENCE".equalsIgnoreCase(portefeuille.getType())) {
            Long agencePortefeuilleId = portefeuille.getAgence() != null ? portefeuille.getAgence().getId() : null;
            Long agenceBienId = bien.getAgenceId();

            if (agencePortefeuilleId == null || agenceBienId == null
                    || !Objects.equals(agencePortefeuilleId, agenceBienId)) {
                throw new ForbiddenException("Ce bien n'appartient pas à la même agence que le portefeuille.");
            }
        }

        // 6) Vérifier si le bien est déjà rattaché à un autre portefeuille
        if (bien.getPortefeuilleId() != null
                && !Objects.equals(bien.getPortefeuilleId(), portefeuilleId)) {
            throw new ConflictException("Ce bien est déjà rattaché à un autre portefeuille.");
        }

        // 7) Lier le bien au portefeuille
        bien.setPortefeuilleId(portefeuilleId);
        bienRepository.save(bien);

        return new ApiMessageResponse("Le bien a été ajouté au portefeuille.");
    }

    @Override
    public ApiMessageResponse ajouterMembreAuPortefeuille(Long portefeuilleId,
                                                          PortefeuilleAjouterMembreRequest request,
                                                          Long currentUserId) {

        PortefeuilleEntity portefeuille = portefeuilleRepository.findById(portefeuilleId)
                .orElseThrow(() -> new NotFoundException("Portefeuille non trouvé."));

        if (!portefeuille.isEnabled()) {
            throw new ForbiddenException("Ce portefeuille est désactivé.");
        }

        // 1) Vérifier que l'appelant est membre + admin du portefeuille
        var appelant = portefeuilleMembreRepository
                .findByPortefeuilleIdAndUtilisateurIdAndEnabledTrue(portefeuilleId, currentUserId)
                .orElseThrow(() ->
                        new ForbiddenException("Vous n'êtes pas membre de ce portefeuille.")
                );

        if (!aDroitAdminPortefeuille(appelant)) {
            throw new ForbiddenException("Vous n'avez pas les droits pour gérer les membres de ce portefeuille.");
        }

        Long utilisateurId = request.utilisateurId();
        String role = request.roleDansPortefeuille();
        if (role == null || role.isBlank()) {
            role = "GESTIONNAIRE";   // rôle par défaut
        }

        UtilisateurEntity utilisateur = utilisateurRepository.findById(utilisateurId)
                .orElseThrow(() -> new NotFoundException("Utilisateur à ajouter introuvable."));

        // 2) Si portefeuille d'agence, vérifier que l'utilisateur appartient à la même agence
        if ("AGENCE".equalsIgnoreCase(portefeuille.getType())) {
            Long agenceId = portefeuille.getAgence() != null ? portefeuille.getAgence().getId() : null;

            if (agenceId == null
                    || !utilisateurAgenceRepository.existsByUtilisateurIdAndAgenceIdAndEnabledTrue(utilisateurId, agenceId)) {
                throw new ForbiddenException("Cet utilisateur n'appartient pas à l'agence de ce portefeuille.");
            }
        }

        // 3) Vérifier si un lien existe déjà (⚠️ sans filtre sur enabled)
        var membreOpt = portefeuilleMembreRepository
                .findByPortefeuilleIdAndUtilisateurId(portefeuilleId, utilisateurId);

        OffsetDateTime now = OffsetDateTime.now();
        PortefeuilleMembreEntity membre;

        if (membreOpt.isPresent()) {
            // 👉 On réactive / met à jour via le mapper
            membre = membreOpt.get();
            portefeuilleMembreMapper.reactiverOuMettreAJour(membre, role, now);
        } else {
            // 👉 On crée via le mapper
            membre = portefeuilleMembreMapper.createMembre(portefeuille, utilisateur, role, now);
        }

        portefeuilleMembreRepository.save(membre);

        return new ApiMessageResponse("L'utilisateur a été ajouté au portefeuille.");
    }


    @Override
    public ApiMessageResponse retirerMembreDuPortefeuille(Long portefeuilleId,
                                                          Long utilisateurId,
                                                          Long currentUserId) {

        // 1) Vérifier que l'appelant est membre admin du portefeuille
        var appelant = portefeuilleMembreRepository
                .findByPortefeuilleIdAndUtilisateurIdAndEnabledTrue(portefeuilleId, currentUserId)
                .orElseThrow(() ->
                        new ForbiddenException("Vous n'êtes pas membre de ce portefeuille.")
                );

        if (!aDroitAdminPortefeuille(appelant)) {
            throw new ForbiddenException("Vous n'avez pas les droits pour gérer les membres de ce portefeuille.");
        }

        // 2) Chercher le membre à retirer
        var membreOpt = portefeuilleMembreRepository
                .findByPortefeuilleIdAndUtilisateurIdAndEnabledTrue(portefeuilleId, utilisateurId);

        if (membreOpt.isEmpty()) {
            return new ApiMessageResponse("Cet utilisateur n'est pas membre actif de ce portefeuille.");
        }

        var membre = membreOpt.get();

        // (Optionnel) Empêcher de retirer le dernier GESTIONNAIRE → à voir plus tard

        membre.setEnabled(false);
        portefeuilleMembreRepository.save(membre);

        return new ApiMessageResponse("L'utilisateur a été retiré du portefeuille.");
    }

    @Override
    @Transactional(readOnly = true)
    public List<BienResponse> listerBiensDuPortefeuille(Long portefeuilleId, Long userId) {
        // Sécurité
        if (!portefeuilleMembreRepository.existsByPortefeuilleIdAndUtilisateurIdAndEnabledTrue(portefeuilleId, userId)) {
            throw new ForbiddenException("Accès refusé au portefeuille.");
        }

        // Récupération SQL
        List<BienEntity> biens = bienRepository.findByPortefeuilleIdAndEnabledTrue(portefeuilleId);

        // Mapping LÉGER (Summary)
        return biens.stream()
                .map(bienResponseMapper::toBienSummary) // 👈 Appel direct au nouveau mapper
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<BienResponse> listerBiensDisponibles(Long userId) {
        UtilisateurAgenceEntity lien = utilisateurAgenceRepository
                .findFirstByUtilisateurIdAndEnabledTrue(userId)
                .orElseThrow(() -> new ForbiddenException("Aucune agence."));

        // Récupération SQL
        List<BienEntity> orphelins = bienRepository
                .findByAgenceIdAndPortefeuilleIdIsNullAndEnabledTrueAndEstUniteLocativeTrue(lien.getAgence().getId());

        // Mapping LÉGER (Summary)
        return orphelins.stream()
                .map(bienResponseMapper::toBienSummary) // 👈 Appel direct au nouveau mapper
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortefeuilleMembreResponse> listerMembres(Long portefeuilleId, Long currentUserId) {
        // 1. Vérifier que je suis membre
        if (!portefeuilleMembreRepository.existsByPortefeuilleIdAndUtilisateurIdAndEnabledTrue(portefeuilleId, currentUserId)) {
            throw new ForbiddenException("Accès refusé.");
        }

        // 2. Récupérer les membres actifs
        var membres = portefeuilleMembreRepository.findByPortefeuilleIdAndEnabledTrue(portefeuilleId);

        // 3. Mapper
        return membres.stream()
                .map(m -> new PortefeuilleMembreResponse(
                        m.getUtilisateur().getId(),
                        m.getUtilisateur().getNom(),
                        m.getUtilisateur().getPrenom(),
                        m.getUtilisateur().getEmail(),
                        m.getRoleDansPortefeuille()
                ))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PortefeuilleMembreResponse> rechercherCollegues(Long agenceId, String query) {
        // Simple mapping
        return utilisateurAgenceRepository.searchMembresAgence(agenceId, query).stream()
                .map(u -> new PortefeuilleMembreResponse(u.getId(), u.getNom(), u.getPrenom(), u.getEmail(), "COLLABORATEUR"))
                .toList();
    }

    private boolean aDroitAdminAgence(UtilisateurAgenceEntity lien) {
        String role = lien.getRoleDansAgence();
        return "ADMIN_AGENCE".equals(role)
                || "RESPONSABLE".equals(role);  // ou "RESPONSABLE_AGENCE" selon ton enum
    }

    private boolean aDroitAdminPortefeuille(PortefeuilleMembreEntity membre) {
        String role = membre.getRoleDansPortefeuille();
        // Tu adapteras si ton enum contient d'autres valeurs (ADMIN, OWNER, etc.)
        return "GESTIONNAIRE".equals(role) || "ADMIN".equals(role);
    }



}
