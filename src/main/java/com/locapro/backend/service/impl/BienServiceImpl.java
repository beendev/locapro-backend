package com.locapro.backend.service.impl;

import com.locapro.backend.domain.context.TravailContext;
import com.locapro.backend.dto.bien.*;
import com.locapro.backend.entity.*;
import com.locapro.backend.exception.ForbiddenException;
import com.locapro.backend.exception.NotFoundException;
import com.locapro.backend.mapper.BienDetailsMapper;
import com.locapro.backend.mapper.BienEntityMapper;
import com.locapro.backend.mapper.BienResponseMapper;
import com.locapro.backend.mapper.ProprietaireMapper;
import com.locapro.backend.repository.*;
import com.locapro.backend.service.BienService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BienServiceImpl implements BienService {

    // --- Repositories ---
    private final BienRepository bienRepository;
    private final DetailsResidentielRepository detailsResidentielRepository;
    private final DetailsCommerceRepository detailsCommerceRepository;
    private final DetailsBureauRepository detailsBureauRepository;
    private final DetailsParkingRepository detailsParkingRepository;
    private final ProprietaireBienRepository proprietaireBienRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurAgenceRepository utilisateurAgenceRepository;

    // --- Mappers ---
    private final BienDetailsMapper bienDetailsMapper;
    private final BienResponseMapper bienResponseMapper;
    private final BienEntityMapper bienEntityMapper;
    private final ProprietaireMapper proprietaireMapper;

    public BienServiceImpl(
            BienRepository bienRepository,
            DetailsResidentielRepository detailsResidentielRepository,
            DetailsCommerceRepository detailsCommerceRepository,
            DetailsBureauRepository detailsBureauRepository,
            DetailsParkingRepository detailsParkingRepository,
            ProprietaireBienRepository proprietaireBienRepository,
            UtilisateurRepository utilisateurRepository,
            UtilisateurAgenceRepository utilisateurAgenceRepository,
            BienDetailsMapper bienDetailsMapper,
            BienResponseMapper bienResponseMapper,
            BienEntityMapper bienEntityMapper,
            ProprietaireMapper proprietaireMapper
    ) {
        this.bienRepository = bienRepository;
        this.detailsResidentielRepository = detailsResidentielRepository;
        this.detailsCommerceRepository = detailsCommerceRepository;
        this.detailsBureauRepository = detailsBureauRepository;
        this.detailsParkingRepository = detailsParkingRepository;
        this.proprietaireBienRepository = proprietaireBienRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurAgenceRepository = utilisateurAgenceRepository;
        this.bienDetailsMapper = bienDetailsMapper;
        this.bienResponseMapper = bienResponseMapper;
        this.bienEntityMapper = bienEntityMapper;
        this.proprietaireMapper = proprietaireMapper;
    }

    // ========================================================================
    // Création standard (1 parent + 1 unité locative)
    // ========================================================================

    @Override
    @Transactional
    public BienResponse creerBien(BienCreationRequest request, Long currentUserId, TravailContext contexte) {

        BienEntity parent;

        // --- 1. GESTION INTELLIGENTE DU PARENT ---
        if (request.existingParentId() != null) {
            // CAS B : On se rattache à un immeuble existant
            parent = bienRepository.findById(request.existingParentId())
                    .orElseThrow(() -> new NotFoundException("Parent existant introuvable : " + request.existingParentId()));

            // Vérif de sécurité : est-ce que j'ai le droit de toucher à ce parent ? (Agence/Perso)
            checkAccessToBien(parent, currentUserId, contexte);

        } else {
            // CAS A : On crée un nouveau bâtiment (Ton code actuel)
            if (request.parent() == null) {
                throw new IllegalArgumentException("Si aucun parent existant n'est sélectionné, les infos du nouveau parent sont obligatoires.");
            }

            Long agenceId = (contexte == TravailContext.AGENCE) ? resolveAgenceIdForUser(currentUserId) : null;

            parent = bienEntityMapper.toParentEntity(request.parent(), agenceId);
            parent = bienRepository.save(parent);

            // On lie le proprio du parent
            upsertLienProprietaire(parent, request.proprietaireParent(), currentUserId);
        }

        // --- 2. CRÉATION DE L'UNITÉ (ENFANT) ---
        // On récupère l'agence du parent pour rester cohérent
        Long agenceId = parent.getAgenceId();

        BienEntity enfant = bienEntityMapper.toUniteEntity(request.bien(), parent, agenceId, true);
        enfant = bienRepository.save(enfant);

        upsertDetails(enfant, request.bien(), request.details());
        upsertLienProprietaire(enfant, request.proprietaire(), currentUserId);

        return mapToBienComplet(enfant, parent);
    }

    // ========================================================================
    // Création colocation
    // ========================================================================

    @Override
    @Transactional
    public List<BienResponse> creerColocation(ColocationCreationRequest request,
                                              Long currentUserId,
                                              TravailContext contexte) {

        if (request == null || request.parent() == null
                || request.appartement() == null
                || request.chambres() == null || request.chambres().isEmpty()) {
            throw new IllegalArgumentException("Parent, appartement et au moins une chambre sont obligatoires.");
        }

        final Long agenceId = (contexte == TravailContext.AGENCE)
                ? resolveAgenceIdForUser(currentUserId)
                : null;

        BienParentInfosRequest parentInfosOriginal = request.parent();
        BienInfosDeBaseRequest appartInfos = request.appartement();

        BienParentInfosRequest parentInfosMerged = new BienParentInfosRequest(
                parentInfosOriginal.nomReferenceInterne(),
                parentInfosOriginal.libelleVisible(),
                parentInfosOriginal.typeBien(),
                parentInfosOriginal.sousType(),
                appartInfos.rue(),
                appartInfos.numero(),
                appartInfos.boite(),
                appartInfos.codePostal(),
                appartInfos.ville(),
                appartInfos.pays(),
                appartInfos.latitude(),
                appartInfos.longitude()
        );

        BienEntity parent = bienEntityMapper.toParentEntity(parentInfosMerged, agenceId);
        parent = bienRepository.save(parent);

        var ownership = request.proprietaire();
        upsertLienProprietaire(parent, ownership, currentUserId);

        final BienEntity finalParent = parent;

        BienEntity appart = bienEntityMapper.toUniteEntity(appartInfos, finalParent, agenceId, false);
        appart = bienRepository.save(appart);

        if (request.detailsAppartement() != null) {
            upsertDetails(appart, appartInfos, request.detailsAppartement());
        }

        upsertLienProprietaire(appart, ownership, currentUserId);

        final BienEntity finalAppart = appart;

        return request.chambres().stream()
                .map(chambreReq -> {
                    var sousTypeFinal = (chambreReq.sousType() != null)
                            ? chambreReq.sousType()
                            : com.locapro.backend.domain.context.SousType.CHAMBRE;

                    BienInfosDeBaseRequest chambreInfos = new BienInfosDeBaseRequest(
                            chambreReq.nomReferenceInterne(),
                            chambreReq.libelleVisible(),
                            appartInfos.typeBien(),
                            sousTypeFinal,
                            chambreReq.lotOuUnite(),
                            chambreReq.numeroPorte(),
                            chambreReq.boiteUnite(),
                            true,
                            appartInfos.rue(),
                            appartInfos.numero(),
                            appartInfos.boite(),
                            appartInfos.codePostal(),
                            appartInfos.ville(),
                            appartInfos.pays(),
                            appartInfos.latitude(),
                            appartInfos.longitude()
                    );

                    BienEntity chambre = bienEntityMapper.toUniteEntity(chambreInfos, finalAppart, agenceId, true);
                    chambre = bienRepository.save(chambre);

                    if (chambreReq.details() != null) {
                        upsertDetails(chambre, chambreInfos, chambreReq.details());
                    }

                    var ownerToUse = (chambreReq.proprietaire() != null)
                            ? chambreReq.proprietaire()
                            : request.proprietaire();

                    upsertLienProprietaire(chambre, ownerToUse, currentUserId);

                    return mapToBienComplet(chambre, finalParent);
                })
                .toList();
    }

    // ========================================================================
    // Update
    // ========================================================================

    @Override
    @Transactional
    public BienResponse mettreAJourBien(Long bienId, BienUpdateRequest request, Long currentUserId, TravailContext contexte) {

        // 1. Récupération
        BienEntity unite = bienRepository.findById(bienId)
                .orElseThrow(() -> new NotFoundException("Bien introuvable: " + bienId));

        // 2. Sécurité
        checkAccessToBien(unite, currentUserId, contexte);

        // 3. Update du Parent (Si demandé)
        BienEntity parent = null;
        if (unite.getParentBienId() != null) {
            parent = bienRepository.findById(unite.getParentBienId()).orElse(null);
            if (parent != null && request.parent() != null) {
                // Attention : Modifier le parent impacte tous les enfants !
                bienEntityMapper.updateParent(parent, request.parent());
                bienRepository.save(parent);
            }
        }

        // 4. Update de l'Unité (Nom, Adresse...)
        if (request.bien() != null) {
            bienEntityMapper.updateUnite(unite, request.bien());
            bienRepository.save(unite);
        }

        // 5. Update des Détails Techniques (Surface, Chauffage...)
        if (request.details() != null) {
            // On récupère le type réel du bien (ex: RESIDENTIEL)
            // Si le front l'envoie, on l'utilise, sinon on garde celui en base
            com.locapro.backend.domain.context.TypeBien typeAUtiliser;
            if (request.bien() != null && request.bien().typeBien() != null) {
                typeAUtiliser = request.bien().typeBien();
            } else {
                typeAUtiliser = com.locapro.backend.domain.context.TypeBien.valueOf(unite.getTypeBien());
            }

            // Appel au mapper qu'on vient de corriger
            upsertDetailsDirect(unite, typeAUtiliser, request.details());
        }

        // 6. Update du Propriétaire (Celui qui manquait !)
        if (request.proprietaire() != null) {
            upsertLienProprietaire(unite, request.proprietaire(), currentUserId);
        }

        return mapToBienComplet(unite, parent);
    }
    // ========================================================================
    // Lecture / listes
    // ========================================================================

    @Override
    @Transactional(readOnly = true)
    public BienEntity getBienById(Long id) {
        return bienRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Bien introuvable: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BienResponse> listerBiensAgence(Long currentUserId) {
        Long agenceId = resolveAgenceIdForUser(currentUserId);
        List<BienEntity> unites = bienRepository
                .findByAgenceIdAndEnabledTrue(agenceId);
        return buildResponseList(unites);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BienResponse> listerBiensPerso(Long currentUserId) {
        var unites = bienRepository.findAllByProprietaireUtilisateur(currentUserId);
        return buildResponseList(unites);
    }

    @Override
    @Transactional(readOnly = true)
    public BienArborescenceResponse getBienComplet(Long bienId,
                                                   Long currentUserId,
                                                   TravailContext contexte) {

        BienEntity unite = bienRepository.findById(bienId)
                .orElseThrow(() -> new NotFoundException("Bien non trouvé : " + bienId));

        checkAccessToBien(unite, currentUserId, contexte);

        List<BienEntity> parentsChain = new ArrayList<>();
        BienEntity current = unite;
        while (current.getParentBienId() != null) {
            Long parentId = current.getParentBienId();
            BienEntity parent = bienRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalStateException("Parent introuvable " + parentId));
            parentsChain.add(parent);
            current = parent;
        }
        Collections.reverse(parentsChain);

        BienEntity parentDirect = parentsChain.isEmpty() ? null : parentsChain.getLast();
        BienResponse uniteDto = mapToBienComplet(unite, parentDirect);
        List<BienResponse> parentsDto = parentsChain.stream()
                .map(parent -> mapToBienComplet(parent, null))
                .toList();

        return new BienArborescenceResponse(uniteDto, parentsDto);
    }

    // ========================================================================
    // Gestion des Détails
    // ========================================================================

    private void upsertDetails(BienEntity bien,
                               BienInfosDeBaseRequest infos,
                               DetailsSpecifiquesRequest d) {
        if (infos == null || d == null) return;
        upsertDetailsDirect(bien, infos.typeBien(), d);
    }

    private void upsertDetailsDirect(BienEntity bien,
                                     com.locapro.backend.domain.context.TypeBien type,
                                     DetailsSpecifiquesRequest d) {
        if (type == null || d == null) return;

        switch (type) {
            case RESIDENTIEL -> upsertDetailsResidentielEtParking(bien, d);

            case COMMERCE -> {
                var existing = detailsCommerceRepository.findById(bien.getId()).orElse(null);
                if (existing != null) {
                    bienDetailsMapper.updateDetailsCommerceEntity(d, existing);
                    detailsCommerceRepository.save(existing);
                } else {
                    var created = bienDetailsMapper.toDetailsCommerceEntity(d, bien);
                    if (created != null) detailsCommerceRepository.save(created);
                }
            }

            case BUREAU -> {
                var existing = detailsBureauRepository.findById(bien.getId()).orElse(null);
                if (existing != null) {
                    bienDetailsMapper.updateDetailsBureauEntity(d, existing);
                    detailsBureauRepository.save(existing);
                } else {
                    var created = bienDetailsMapper.toDetailsBureauEntity(d, bien);
                    if (created != null) detailsBureauRepository.save(created);
                }
            }

            case PARKING -> {
                var existing = detailsParkingRepository.findById(bien.getId()).orElse(null);
                if (existing != null) {
                    bienDetailsMapper.updateDetailsParkingEntity(d, existing);
                    detailsParkingRepository.save(existing);
                } else {
                    var created = bienDetailsMapper.toDetailsParkingEntity(d, bien);
                    if (created != null) detailsParkingRepository.save(created);
                }
            }
        }
    }

    private void upsertDetailsResidentielEtParking(BienEntity bien, DetailsSpecifiquesRequest d) {
        var existingRes = detailsResidentielRepository.findById(bien.getId()).orElse(null);
        if (existingRes != null) {
            bienDetailsMapper.updateDetailsResidentielEntity(d, existingRes);
            detailsResidentielRepository.save(existingRes);
        } else {
            var created = bienDetailsMapper.toDetailsResidentielEntity(d, bien);
            if (created != null) detailsResidentielRepository.save(created);
        }

        var existingPark = detailsParkingRepository.findById(bien.getId()).orElse(null);
        if (bienDetailsMapper.hasParkingInfo(d)) {
            if (existingPark != null) {
                bienDetailsMapper.updateDetailsParkingEntity(d, existingPark);
                detailsParkingRepository.save(existingPark);
            } else {
                var createdPark = bienDetailsMapper.toDetailsParkingEntity(d, bien);
                if (createdPark != null) detailsParkingRepository.save(createdPark);
            }
        } else {
            if (existingPark != null) detailsParkingRepository.delete(existingPark);
        }
    }

    // ========================================================================
    // Gestion Propriétaire
    // ========================================================================

    private void upsertLienProprietaire(BienEntity bien,
                                        BienOwnershipRequest ownership,
                                        Long currentUserId) {
        if (ownership == null || ownership.mode() == null) return;

        var existingOpt = proprietaireBienRepository
                .findFirstByBienIdAndEnabledTrueOrderByIdAsc(bien.getId());

        ProprietaireBienEntity lien = existingOpt.orElseGet(ProprietaireBienEntity::new);
        lien.setBienId(bien.getId());
        lien.setEnabled(true);

        UtilisateurEntity currentUser = null;
        if (ownership.mode() == com.locapro.backend.domain.context.BienOwnershipMode.SELF) {
            currentUser = utilisateurRepository.findById(currentUserId)
                    .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        }

        proprietaireMapper.applyOwnership(lien, ownership, currentUser);
        proprietaireBienRepository.save(lien);
    }

    // ========================================================================
    // Helpers Utilitaires
    // ========================================================================

    private List<BienResponse> buildResponseList(List<BienEntity> unites) {
        Set<Long> parentIds = unites.stream()
                .map(BienEntity::getParentBienId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<Long, BienEntity> parentsById = parentIds.isEmpty()
                ? Map.of()
                : bienRepository.findAllById(parentIds).stream()
                .collect(Collectors.toMap(BienEntity::getId, Function.identity()));

        return unites.stream()
                .map(unite -> mapToBienComplet(unite, parentsById.get(unite.getParentBienId())))
                .toList();
    }

    private Long resolveAgenceIdForUser(Long currentUserId) {
        var liens = utilisateurAgenceRepository
                .findByUtilisateurIdAndEnabledTrue(currentUserId);
        return liens.stream().findFirst().map(ua -> {
            var agence = ua.getAgence();
            return agence != null ? agence.getId() : null;
        }).orElseThrow(() -> new ForbiddenException("Vous n'êtes rattaché à aucune agence active."));
    }

    private void checkAccessToBien(BienEntity bien, Long currentUserId, TravailContext contexte) {
        Long bienId = bien.getId();
        if (contexte == TravailContext.PERSONNEL) {
            boolean isOwner = proprietaireBienRepository
                    .existsByBienIdAndProprietaireUtilisateurIdAndEnabledTrue(bienId, currentUserId);
            if (!isOwner) throw new ForbiddenException("Accès refusé (Personnel).");
            return;
        }
        if (contexte == TravailContext.AGENCE) {
            Long agenceId = bien.getAgenceId();
            if (agenceId == null) throw new ForbiddenException("Ce bien n'est lié à aucune agence.");
            boolean hasLien = utilisateurAgenceRepository
                    .existsByUtilisateurIdAndAgenceIdAndEnabledTrue(currentUserId, agenceId);
            if (!hasLien) throw new ForbiddenException("Accès refusé (Agence).");
            return;
        }
        throw new ForbiddenException("Contexte invalide.");
    }

    private BienResponse mapToBienComplet(BienEntity unite, BienEntity parent) {
        Long id = unite.getId();
        var res = detailsResidentielRepository.findById(id).orElse(null);
        var com = detailsCommerceRepository.findById(id).orElse(null);
        var bur = detailsBureauRepository.findById(id).orElse(null);
        var park = detailsParkingRepository.findById(id).orElse(null);

        var propEnfant = proprietaireBienRepository
                .findFirstByBienIdAndEnabledTrueOrderByIdAsc(id)
                .orElse(null);

        ProprietaireBienEntity propParent = null;
        if (parent != null) {
            propParent = proprietaireBienRepository
                    .findFirstByBienIdAndEnabledTrueOrderByIdAsc(parent.getId())
                    .orElse(null);
        }

        return bienResponseMapper.toBienComplet(unite, parent, res, com, bur, park, propEnfant, propParent);
    }
}