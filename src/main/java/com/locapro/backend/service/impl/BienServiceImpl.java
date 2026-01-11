package com.locapro.backend.service.impl;

import com.locapro.backend.domain.context.BienOwnershipMode;
import com.locapro.backend.domain.context.TravailContext;
import com.locapro.backend.dto.bien.*;
import com.locapro.backend.entity.*;
import com.locapro.backend.exception.ForbiddenException;
import com.locapro.backend.exception.NotFoundException;
import com.locapro.backend.mapper.BienDetailsMapper;
import com.locapro.backend.mapper.BienResponseMapper;
import com.locapro.backend.repository.*;
import com.locapro.backend.service.BienService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BienServiceImpl implements BienService {

    private final BienRepository bienRepository;
    private final DetailsResidentielRepository detailsResidentielRepository;
    private final DetailsCommerceRepository detailsCommerceRepository;
    private final DetailsBureauRepository detailsBureauRepository;
    private final DetailsParkingRepository detailsParkingRepository;
    private final ProprietaireBienRepository proprietaireBienRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurAgenceRepository utilisateurAgenceRepository;

    private final BienDetailsMapper bienDetailsMapper;
    private final BienResponseMapper bienResponseMapper;

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
            BienResponseMapper bienResponseMapper
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
    }

// ========================================================================
// Création standard (1 parent + 1 unité locative)
// ========================================================================

    @Override
    @Transactional
    public BienResponse creerBien(BienCreationRequest request,
                                  Long currentUserId,
                                  TravailContext contexte) {

        if (request.parent() == null) {
            throw new IllegalArgumentException(
                    "Un parent est obligatoire pour créer une unité locative."
            );
        }

        Long agenceId = null;
        if (contexte == TravailContext.AGENCE) {
            agenceId = resolveAgenceIdForUser(currentUserId);
        }

        // 1) Parent (immeuble / maison, non louable)
        BienEntity parent = creerParent(request, agenceId);

        // 2) Enfant (unité locative)
        BienEntity enfant = creerEnfant(request, parent, agenceId);

        // 3) Détails selon type + éventuel parking intégré (UPSERT)
        upsertDetails(enfant, request.bien(), request.details());

        // 4) Lien propriétaire snapshot (UPSERT)
        upsertLienProprietaire(enfant, request.proprietaire(), currentUserId);

        // 5) Retourne le bien complet
        return mapToBienComplet(enfant, parent);
    }

// ========================================================================
// Création colocation (1 parent + 1 appart non locatif + N chambres locatives)
// ========================================================================

    @Override
    @Transactional
    public List<BienResponse> creerColocation(ColocationCreationRequest request,
                                              Long currentUserId,
                                              TravailContext contexte) {

        if (request == null
                || request.parent() == null
                || request.appartement() == null
                || request.chambres() == null
                || request.chambres().isEmpty()) {
            throw new IllegalArgumentException(
                    "Parent, appartement et au moins une chambre sont obligatoires pour une colocation."
            );
        }

        final Long agenceId = (contexte == TravailContext.AGENCE)
                ? resolveAgenceIdForUser(currentUserId)
                : null;

        // 1) Parent (immeuble / maison)
        var parentInfos = request.parent();
        var appartInfos = request.appartement();

        BienEntity parent = new BienEntity();
        parent.setNomReference(parentInfos.nomReferenceInterne());
        parent.setLibelleUnite(parentInfos.libelleVisible());
        parent.setTypeBien(parentInfos.typeBien().name());
        parent.setSousType(parentInfos.sousType());
        parent.setStatut("ACTIF");
        parent.setEstUniteLocative(false);
        parent.setParentBienId(null);
        parent.setEnabled(true);
        parent.setPortefeuilleId(null);

        // Adresse: on réutilise l’adresse de l’appartement,
        // comme pour creerParent(BienCreationRequest)
        parent.setRue(appartInfos.rue());
        parent.setNumero(appartInfos.numero());
        parent.setBoite(appartInfos.boite());
        parent.setCodePostal(appartInfos.codePostal());
        parent.setVille(appartInfos.ville());
        parent.setPays(appartInfos.pays());
        parent.setLatitude(appartInfos.latitude());
        parent.setLongitude(appartInfos.longitude());

        parent.setAgenceId(agenceId);

        parent = bienRepository.save(parent);

        // 👇 variable finale pour la lambda
        final BienEntity finalParent = parent;

        // 2) Appartement non locatif (enfant du parent)
        BienEntity appart = creerUnite(appartInfos, finalParent, agenceId, false);

// Détails de l’appartement
        if (request.detailsAppartement() != null) {
            upsertDetails(appart, appartInfos, request.detailsAppartement());
        }

// 👇 ajout : variable finale pour la lambda
        final BienEntity finalAppart = appart;

// 3) Chambres locatives (enfants de l’appartement)
        var ownership = request.proprietaire();

        return request.chambres().stream()
                .map(chambreReq -> {
                    BienInfosDeBaseRequest chambreInfos = new BienInfosDeBaseRequest(
                            chambreReq.nomReferenceInterne(),
                            chambreReq.libelleVisible(),
                            appartInfos.typeBien(),
                            appartInfos.sousType(),
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

                    // ⚠️ ICI : parent = finalAppart (l’appartement), plus la maison
                    BienEntity chambre = creerUnite(chambreInfos, finalAppart, agenceId, true);

                    if (chambreReq.details() != null) {
                        upsertDetails(chambre, chambreInfos, chambreReq.details());
                    }

                    if (ownership != null && ownership.mode() != null) {
                        upsertLienProprietaire(chambre, ownership, currentUserId);
                    }

                    // on renvoie toujours chambre + parent immeuble (si tu veux changer ça plus tard, on verra)
                    return mapToBienComplet(chambre, finalParent);
                })
                .toList();

    }


// ========================================================================
// Création parent / enfant
// ========================================================================

    private BienEntity creerParent(BienCreationRequest request, Long agenceId) {
        var parentInfos = request.parent();
        var infos = request.bien(); // on réutilise l’adresse du bien

        BienEntity parent = new BienEntity();
        parent.setNomReference(parentInfos.nomReferenceInterne());
        parent.setLibelleUnite(parentInfos.libelleVisible());
        parent.setTypeBien(parentInfos.typeBien().name());
        parent.setSousType(parentInfos.sousType());
        parent.setStatut("ACTIF");
        parent.setEstUniteLocative(false);
        parent.setParentBienId(null);
        parent.setEnabled(true);
        parent.setPortefeuilleId(null); // pas encore utilisé

        // Adresse
        parent.setRue(infos.rue());
        parent.setNumero(infos.numero());
        parent.setBoite(infos.boite());
        parent.setCodePostal(infos.codePostal());
        parent.setVille(infos.ville());
        parent.setPays(infos.pays());
        parent.setLatitude(infos.latitude());
        parent.setLongitude(infos.longitude());

        parent.setAgenceId(agenceId);

        return bienRepository.save(parent);
    }

    private BienEntity creerEnfant(BienCreationRequest request,
                                   BienEntity parent,
                                   Long agenceId) {
        var infos = request.bien();
        return creerUnite(infos, parent, agenceId, true);
    }

    // Helper générique pour créer une unité (locative ou non)
    private BienEntity creerUnite(BienInfosDeBaseRequest infos,
                                  BienEntity parent,
                                  Long agenceId,
                                  boolean estUniteLocative) {

        BienEntity bien = new BienEntity();
        bien.setNomReference(infos.nomReferenceInterne());
        bien.setLibelleUnite(infos.libelleVisible());
        bien.setTypeBien(infos.typeBien().name());
        bien.setSousType(infos.sousType());
        bien.setNumeroPorte(infos.numeroPorte());
        bien.setBoiteUnite(infos.boiteUnite());
        bien.setEstUniteLocative(estUniteLocative);
        bien.setParentBienId(parent != null ? parent.getId() : null);
        bien.setEnabled(true);
        bien.setStatut("ACTIF");

        // Adresse: portée par le parent dans ta logique, mais on garde agenceId
        bien.setAgenceId(agenceId);

        return bienRepository.save(bien);
    }

// ========================================================================
// UPSERT Détails (résidentiel / commerce / bureau / parking + parking intégré)
// ========================================================================

    private void upsertDetails(BienEntity bien,
                               BienInfosDeBaseRequest infos,
                               DetailsSpecifiquesRequest d) {

        if (infos == null || d == null) return;

        switch (infos.typeBien()) {
            case RESIDENTIEL -> upsertDetailsResidentielEtParking(bien, d);

            case COMMERCE -> {
                var existing = detailsCommerceRepository.findById(bien.getId()).orElse(null);
                if (existing != null) {
                    bienDetailsMapper.updateDetailsCommerceEntity(d, existing);
                    detailsCommerceRepository.save(existing);
                } else {
                    var created = bienDetailsMapper.toDetailsCommerceEntity(d, bien);
                    if (created != null) {
                        detailsCommerceRepository.save(created);
                    }
                }
            }

            case BUREAU -> {
                var existing = detailsBureauRepository.findById(bien.getId()).orElse(null);
                if (existing != null) {
                    bienDetailsMapper.updateDetailsBureauEntity(d, existing);
                    detailsBureauRepository.save(existing);
                } else {
                    var created = bienDetailsMapper.toDetailsBureauEntity(d, bien);
                    if (created != null) {
                        detailsBureauRepository.save(created);
                    }
                }
            }

            case PARKING -> {
                var existing = detailsParkingRepository.findById(bien.getId()).orElse(null);
                if (existing != null) {
                    bienDetailsMapper.updateDetailsParkingEntity(d, existing);
                    detailsParkingRepository.save(existing);
                } else {
                    var created = bienDetailsMapper.toDetailsParkingEntity(d, bien);
                    if (created != null) {
                        detailsParkingRepository.save(created);
                    }
                }
            }
        }
    }

    private void upsertDetailsResidentielEtParking(BienEntity bien, DetailsSpecifiquesRequest d) {
        // Résidentiel
        var existingRes = detailsResidentielRepository.findById(bien.getId()).orElse(null);
        if (existingRes != null) {
            bienDetailsMapper.updateDetailsResidentielEntity(d, existingRes);
            detailsResidentielRepository.save(existingRes);
        } else {
            var created = bienDetailsMapper.toDetailsResidentielEntity(d, bien);
            if (created != null) {
                detailsResidentielRepository.save(created);
            }
        }

        // Parking intégré
        var existingPark = detailsParkingRepository.findById(bien.getId()).orElse(null);

        if (bienDetailsMapper.hasParkingInfo(d)) {
            if (existingPark != null) {
                bienDetailsMapper.updateDetailsParkingEntity(d, existingPark);
                detailsParkingRepository.save(existingPark);
            } else {
                var createdPark = bienDetailsMapper.toDetailsParkingEntity(d, bien);
                if (createdPark != null) {
                    detailsParkingRepository.save(createdPark);
                }
            }
        } else {
            if (existingPark != null) {
                detailsParkingRepository.delete(existingPark);
            }
        }
    }

// ========================================================================
// UPSERT Propriétaire snapshot
// ========================================================================

    private void upsertLienProprietaire(BienEntity bien,
                                        BienOwnershipRequest ownership,
                                        Long currentUserId) {

        if (ownership == null || ownership.mode() == null) {
            return;
        }

        var existingOpt = proprietaireBienRepository
                .findFirstByBienIdAndEnabledTrueOrderByIdAsc(bien.getId());

        ProprietaireBienEntity lien = existingOpt.orElseGet(ProprietaireBienEntity::new);
        lien.setBienId(bien.getId());
        lien.setEnabled(true);

        appliquerOwnershipSurLien(ownership, lien, currentUserId);

        proprietaireBienRepository.save(lien);
    }

    private void appliquerOwnershipSurLien(BienOwnershipRequest ownership,
                                           ProprietaireBienEntity lien,
                                           Long currentUserId) {

        BienOwnershipMode mode = ownership.mode();

        switch (mode) {
            case SELF -> {
                var u = utilisateurRepository.findById(currentUserId)
                        .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

                lien.setProprietaireType("PERSONNE");
                lien.setProprietaireUtilisateurId(u.getId());
                lien.setProprietaireNom(u.getNom());
                lien.setProprietairePrenom(u.getPrenom());
                lien.setProprietaireEmail(u.getEmail());
                lien.setProprietaireEntrepriseNom(null);
            }
            case PERSONNE_PHYSIQUE -> {
                var p = ownership.personne();
                if (p != null) {
                    lien.setProprietaireType("PERSONNE");
                    lien.setProprietaireUtilisateurId(null);
                    lien.setProprietaireNom(p.nom());
                    lien.setProprietairePrenom(p.prenom());
                    lien.setProprietaireEmail(p.email());
                    lien.setProprietaireEntrepriseNom(null);
                }
            }
            case ENTREPRISE -> {
                var e = ownership.entreprise();
                if (e != null) {
                    lien.setProprietaireType("ENTREPRISE");
                    lien.setProprietaireUtilisateurId(null);
                    lien.setProprietaireNom(null);
                    lien.setProprietairePrenom(null);
                    lien.setProprietaireEmail(e.email());
                    lien.setProprietaireEntrepriseNom(e.raisonSociale());
                }
            }
        }
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
                .findByAgenceIdAndEnabledTrueAndEstUniteLocativeTrue(agenceId);

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

    @Override
    @Transactional(readOnly = true)
    public BienArborescenceResponse getBienComplet(Long bienId,
                                                   Long currentUserId,
                                                   TravailContext contexte) {

        // 1) On récupère l’unité demandée
        BienEntity unite = bienRepository.findById(bienId)
                .orElseThrow(() -> new NotFoundException("Bien non trouvé : " + bienId));

        // 1bis) Vérification des droits d'accès (perso / agence)
        checkAccessToBien(unite, currentUserId, contexte);

        // 2) On remonte toute la chaîne des parents
        List<BienEntity> parentsChain = new ArrayList<>();

        BienEntity current = unite;
        while (current.getParentBienId() != null) {
            Long parentId = current.getParentBienId();

            BienEntity parent = bienRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Parent introuvable pour le bien " + parentId
                    ));

            parentsChain.add(parent);
            current = parent;
        }

        Collections.reverse(parentsChain);

        BienEntity parentDirect = parentsChain.isEmpty()
                ? null
                : parentsChain.get(parentsChain.size() - 1);

        BienResponse uniteDto = bienResponseMapper.toBienComplet(unite, parentDirect);

        List<BienResponse> parentsDto = parentsChain.stream()
                .map(parent -> bienResponseMapper.toBienComplet(parent, null))
                .toList();

        return new BienArborescenceResponse(uniteDto, parentsDto);
    }


    @Override
    @Transactional(readOnly = true)
    public List<BienResponse> listerBiensPerso(Long currentUserId) {
        var unites = bienRepository.findAllByProprietaireUtilisateur(currentUserId);

        List<Long> parentIds = unites.stream()
                .map(BienEntity::getParentBienId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, BienEntity> parentsById = parentIds.isEmpty()
                ? Map.of()
                : bienRepository.findAllById(parentIds).stream()
                .collect(Collectors.toMap(BienEntity::getId, Function.identity()));

        return unites.stream()
                .map(unite -> mapToBienComplet(
                        unite,
                        parentsById.get(unite.getParentBienId())
                ))
                .toList();
    }

// ========================================================================
// Update
// ========================================================================

    @Override
    @Transactional
    public BienResponse mettreAJourBien(Long bienId,
                                        BienUpdateRequest request,
                                        Long currentUserId,
                                        TravailContext contexte) {

        BienEntity unite = bienRepository.findById(bienId)
                .orElseThrow(() -> new RuntimeException("Bien introuvable: " + bienId));

        // 🔒 Vérifier l'accès avant de modifier
        checkAccessToBien(unite, currentUserId, contexte);

        BienEntity parent = null;
        if (unite.getParentBienId() != null) {
            parent = bienRepository.findById(unite.getParentBienId()).orElse(null);
        }

        // 1) Parent
        appliquerModificationsParent(parent, request);
        if (parent != null) {
            bienRepository.save(parent);
        }

        // 2) Unité
        appliquerModificationsBien(unite, request);
        bienRepository.save(unite);

        // 3) Détails
        upsertDetails(unite, request.bien(), request.details());

        // 4) Propriétaire
        upsertLienProprietaire(unite, request.proprietaire(), currentUserId);

        return mapToBienComplet(unite, parent);
    }

// ========================================================================
// Helpers update
// ========================================================================

    private void appliquerModificationsParent(BienEntity parent, BienUpdateRequest request) {
        var parentInfos = request.parent();
        if (parent == null || parentInfos == null) return;

        parent.setNomReference(parentInfos.nomReferenceInterne());
        parent.setLibelleUnite(parentInfos.libelleVisible());
        parent.setTypeBien(parentInfos.typeBien().name());
        parent.setSousType(parentInfos.sousType());

        parent.setRue(parentInfos.rue());
        parent.setNumero(parentInfos.numero());
        parent.setBoite(parentInfos.boite());
        parent.setCodePostal(parentInfos.codePostal());
        parent.setVille(parentInfos.ville());
        parent.setPays(parentInfos.pays());
        parent.setLatitude(parentInfos.latitude());
        parent.setLongitude(parentInfos.longitude());
    }

    private void appliquerModificationsBien(BienEntity bien, BienUpdateRequest request) {
        var infos = request.bien();
        if (infos == null) return;

        bien.setNomReference(infos.nomReferenceInterne());
        bien.setLibelleUnite(infos.libelleVisible());
        // Tu peux autoriser ou non le changement de typeBien/sousType ici

        bien.setNumeroPorte(infos.numeroPorte());
        bien.setBoiteUnite(infos.boiteUnite());
    }

// ========================================================================
// Outils
// ========================================================================

    private Long resolveAgenceIdForUser(Long currentUserId) {
        var liens = utilisateurAgenceRepository
                .findByUtilisateurIdAndEnabledTrue(currentUserId);

        return liens.stream()
                .findFirst()
                .map(ua -> {
                    var agence = ua.getAgence();
                    return agence != null ? agence.getId() : null;
                })
                .orElseThrow(() ->
                        new ForbiddenException("Vous n'êtes rattaché à aucune agence active.")
                );
    }
    /**
     * Vérifie que l'utilisateur courant a le droit de voir ce bien.
     * - En contexte AGENCE : il doit appartenir à la même agence que le bien.

     */
    private void checkAccessToBien(BienEntity bien,
                                   Long currentUserId,
                                   TravailContext contexte) {

        Long bienId = bien.getId();

        // 1) Contexte PERSO : l'utilisateur doit être propriétaire du bien
        if (contexte == TravailContext.PERSONNEL) {
            boolean isOwner = proprietaireBienRepository
                    .existsByBienIdAndProprietaireUtilisateurIdAndEnabledTrue(bienId, currentUserId);

            if (!isOwner) {
                throw new ForbiddenException("Vous n'avez pas accès à ce bien dans le contexte personnel.");
            }
            return;
        }

        // 2) Contexte AGENCE : l'utilisateur doit être rattaché à la même agence que le bien
        if (contexte == TravailContext.AGENCE) {

            Long agenceId = bien.getAgenceId();
            if (agenceId == null) {
                throw new ForbiddenException("Ce bien n'est rattaché à aucune agence.");
            }

            boolean hasLien = utilisateurAgenceRepository
                    .existsByUtilisateurIdAndAgenceIdAndEnabledTrue(currentUserId, agenceId);

            if (!hasLien) {
                throw new ForbiddenException("Vous n'avez pas accès à ce bien pour cette agence.");
            }
            return;
        }

        // 3) Contexte inconnu / non géré
        throw new ForbiddenException("Contexte de travail invalide pour l'accès au bien.");
    }


    private BienResponse mapToBienComplet(BienEntity unite, BienEntity parent) {
        return bienResponseMapper.toBienComplet(unite, parent);
    }
}
