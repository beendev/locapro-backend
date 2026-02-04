package com.locapro.backend.service.impl;

import com.locapro.backend.domain.context.Permission;
import com.locapro.backend.domain.context.SousType;
import com.locapro.backend.domain.context.TravailContext;
import com.locapro.backend.domain.context.TypeBien;
import com.locapro.backend.dto.bien.*;
import com.locapro.backend.entity.*;
import com.locapro.backend.exception.ForbiddenException;
import com.locapro.backend.exception.NotFoundException;
import com.locapro.backend.mapper.BienDetailsMapper;
import com.locapro.backend.mapper.BienEntityMapper;
import com.locapro.backend.mapper.BienResponseMapper;
import com.locapro.backend.mapper.ProprietaireMapper;
import com.locapro.backend.repository.*;
import com.locapro.backend.service.AuthorizationService;
import com.locapro.backend.service.BienService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
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
    private final DetailsColocationRepository detailsColocationRepository;
    private final ProprietaireBienRepository proprietaireBienRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurAgenceRepository utilisateurAgenceRepository;

    // --- Services ---
    private final AuthorizationService authorizationService;

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
            DetailsColocationRepository detailsColocationRepository,
            ProprietaireBienRepository proprietaireBienRepository,
            UtilisateurRepository utilisateurRepository,
            UtilisateurAgenceRepository utilisateurAgenceRepository,
            AuthorizationService authorizationService,
            BienDetailsMapper bienDetailsMapper,
            BienResponseMapper bienResponseMapper,
            BienEntityMapper bienEntityMapper,
            ProprietaireMapper proprietaireMapper) {
        this.bienRepository = bienRepository;
        this.detailsResidentielRepository = detailsResidentielRepository;
        this.detailsCommerceRepository = detailsCommerceRepository;
        this.detailsBureauRepository = detailsBureauRepository;
        this.detailsParkingRepository = detailsParkingRepository;
        this.detailsColocationRepository = detailsColocationRepository;
        this.proprietaireBienRepository = proprietaireBienRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.utilisateurAgenceRepository = utilisateurAgenceRepository;
        this.authorizationService = authorizationService;
        this.bienDetailsMapper = bienDetailsMapper;
        this.bienResponseMapper = bienResponseMapper;
        this.bienEntityMapper = bienEntityMapper;
        this.proprietaireMapper = proprietaireMapper;
    }

    // ========================================================================
    // CRÉATION RÉCURSIVE (remplace creerBien + creerColocation)
    // ========================================================================

    @Override
    @Transactional
    public BienResponse creerStructureComplete(BienCreationRequest request,
            Long currentUserId,
            TravailContext contexte) {

        Long agenceId = (contexte == TravailContext.AGENCE)
                ? resolveAgenceIdForUser(currentUserId)
                : null;

        BienEntity root = creerNoeudRecursif(request, null, agenceId, currentUserId, contexte);

        BienEntity parent = root.getParentBienId() != null
                ? bienRepository.findById(root.getParentBienId()).orElse(null)
                : null;

        return mapToBienComplet(root, parent);
    }

    /**
     * Cœur récursif : Sauvegarde le nœud courant → récupère son ID → sauvegarde les
     * enfants.
     */
    private BienEntity creerNoeudRecursif(BienCreationRequest req,
            BienEntity parent,
            Long agenceId,
            Long currentUserId,
            TravailContext contexte) {

        BienEntity entity;

        if (req.existingParentId() != null) {
            // --- Rattachement à un parent existant ---
            BienEntity existingParent = bienRepository.findById(req.existingParentId())
                    .orElseThrow(() -> new NotFoundException(
                            "Parent existant introuvable : " + req.existingParentId()));
            checkAccessToBien(existingParent, currentUserId, contexte);

            // Créer la nouvelle unité et la rattacher au parent existant
            entity = bienEntityMapper.toEntity(req);
            entity.setAgenceId(agenceId);
            entity.setParentBienId(existingParent.getId());

            // Auto-générer le nomReference (identifiant unique)
            String identifiant = req.boiteUnite() != null ? req.boiteUnite() : req.lotOuUnite();
            String nomRefGenere = generateNomReference(req.typeBien(), req.sousType(), identifiant);
            entity.setNomReference(nomRefGenere);

            // Fallback libelleUnite : si non fourni, utiliser le nomReference généré
            if (entity.getLibelleUnite() == null || entity.getLibelleUnite().isBlank()) {
                entity.setLibelleUnite(nomRefGenere);
            }

            // Héritage d'adresse si l'unité n'en a pas
            if (entity.getRue() == null) {
                entity.setRue(existingParent.getRue());
                entity.setNumero(existingParent.getNumero());
                entity.setBoite(existingParent.getBoite());
                entity.setCodePostal(existingParent.getCodePostal());
                entity.setVille(existingParent.getVille());
                entity.setCommune(existingParent.getCommune());
                entity.setPays(existingParent.getPays());
                entity.setLatitude(existingParent.getLatitude());
                entity.setLongitude(existingParent.getLongitude());
            }

            entity = bienRepository.save(entity);

            // Détails techniques
            if (req.details() != null && req.typeBien() != null) {
                upsertDetailsDirect(entity, req.typeBien(), req.details());
            }

            // Propriétaire
            if (req.proprietaire() != null) {
                upsertLienProprietaire(entity, req.proprietaire(), currentUserId);
            }

        } else {
            // --- Création du nœud sans parent existant ---
            entity = bienEntityMapper.toEntity(req);
            entity.setAgenceId(agenceId);
            entity.setParentBienId(parent != null ? parent.getId() : null);

            // Auto-générer le nomReference (identifiant unique)
            String identifiant = req.boiteUnite() != null ? req.boiteUnite() : req.lotOuUnite();
            String nomRefGenere = generateNomReference(req.typeBien(), req.sousType(), identifiant);
            entity.setNomReference(nomRefGenere);

            // Fallback libelleUnite : si non fourni, utiliser le nomReference généré
            if (entity.getLibelleUnite() == null || entity.getLibelleUnite().isBlank()) {
                entity.setLibelleUnite(nomRefGenere);
            }

            // Héritage d'adresse : si l'enfant n'a pas d'adresse, on prend celle du parent
            if (parent != null && entity.getRue() == null) {
                entity.setRue(parent.getRue());
                entity.setNumero(parent.getNumero());
                entity.setBoite(parent.getBoite());
                entity.setCodePostal(parent.getCodePostal());
                entity.setVille(parent.getVille());
                entity.setCommune(parent.getCommune());
                entity.setPays(parent.getPays());
                entity.setLatitude(parent.getLatitude());
                entity.setLongitude(parent.getLongitude());
            }

            entity = bienRepository.save(entity);

            // Détails techniques
            if (req.details() != null && req.typeBien() != null) {
                upsertDetailsDirect(entity, req.typeBien(), req.details());
            }

            // Propriétaire
            if (req.proprietaire() != null) {
                upsertLienProprietaire(entity, req.proprietaire(), currentUserId);
            }
        }

        // --- Récursion sur les sous-biens ---
        if (req.sousBiens() != null) {
            for (BienCreationRequest child : req.sousBiens()) {
                creerNoeudRecursif(child, entity, agenceId, currentUserId, contexte);
            }
        }

        return entity;
    }

    // ========================================================================
    // UPDATE
    // ========================================================================

    @Override
    @Transactional
    public BienResponse mettreAJourBien(Long bienId, BienUpdateRequest request,
            Long currentUserId, TravailContext contexte) {

        BienEntity unite = bienRepository.findById(bienId)
                .orElseThrow(() -> new NotFoundException("Bien introuvable: " + bienId));

        checkAccessToBien(unite, currentUserId, contexte);

        // Update du Parent
        BienEntity parent = null;
        if (unite.getParentBienId() != null) {
            parent = bienRepository.findById(unite.getParentBienId()).orElse(null);
            if (parent != null && request.parent() != null) {
                bienEntityMapper.updateParent(parent, request.parent());
                bienRepository.save(parent);
            }
        }

        // Update de l'Unité
        if (request.bien() != null) {
            bienEntityMapper.updateUnite(unite, request.bien());
            bienRepository.save(unite);

            // Si c'est un parent (bâtiment) et qu'on a modifié l'adresse, propager aux
            // enfants
            if (!unite.isEstUniteLocative()) {
                propagateAddressToChildren(unite);
            }
        }

        // Update des Détails Techniques
        if (request.details() != null) {
            TypeBien typeAUtiliser;
            if (request.bien() != null && request.bien().typeBien() != null) {
                typeAUtiliser = request.bien().typeBien();
            } else {
                typeAUtiliser = TypeBien.valueOf(unite.getTypeBien());
            }
            upsertDetailsDirect(unite, typeAUtiliser, request.details());
        }

        // Update du Propriétaire
        if (request.proprietaire() != null) {
            upsertLienProprietaire(unite, request.proprietaire(), currentUserId);
        }

        return mapToBienComplet(unite, parent);
    }

    // ========================================================================
    // LECTURE / LISTES
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

        // RBAC : Récupérer les portefeuilles accessibles
        List<Long> accessiblePortfolioIds = authorizationService.getAccessiblePortfolioIds(currentUserId, agenceId);

        // Récupérer tous les biens de l'agence (unités locatives uniquement)
        List<BienEntity> unites = bienRepository
                .findByAgenceIdAndEnabledTrueAndEstUniteLocativeTrueOrderByCreeLeDesc(agenceId);

        // Filtrer selon les portefeuilles accessibles (admins voient tout)
        var role = authorizationService.getUserRoleInAgency(currentUserId, agenceId);
        if (!role.hasFullPortfolioAccess()) {
            unites = unites.stream()
                    .filter(bien -> bien.getPortefeuilleId() == null
                            || accessiblePortfolioIds.contains(bien.getPortefeuilleId()))
                    .toList();
        }

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
    public List<BienResponse> listerBatimentsAgence(Long currentUserId) {
        Long agenceId = resolveAgenceIdForUser(currentUserId);

        // RBAC : Récupérer les portefeuilles accessibles
        List<Long> accessiblePortfolioIds = authorizationService.getAccessiblePortfolioIds(currentUserId, agenceId);

        // Récupérer tous les bâtiments de l'agence
        List<BienEntity> batiments = bienRepository
                .findByAgenceIdAndEnabledTrueAndEstUniteLocativeFalseOrderByCreeLeDesc(agenceId);

        // Filtrer selon les portefeuilles accessibles (admins voient tout)
        var role = authorizationService.getUserRoleInAgency(currentUserId, agenceId);
        if (!role.hasFullPortfolioAccess()) {
            batiments = batiments.stream()
                    .filter(bien -> bien.getPortefeuilleId() == null
                            || accessiblePortfolioIds.contains(bien.getPortefeuilleId()))
                    .toList();
        }

        return buildResponseList(batiments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BienResponse> listerBatimentsPerso(Long currentUserId) {
        var batiments = bienRepository.findBuildingsByProprietaireUtilisateur(currentUserId);
        return buildResponseList(batiments);
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
                .map(p -> mapToBienComplet(p, null))
                .toList();

        // Récupérer les sous-biens (enfants) de cette unité
        List<BienEntity> enfants = bienRepository.findByParentBienIdAndEnabledTrue(bienId);
        List<BienResponse> sousBiensDto = enfants.stream()
                .map(e -> mapToBienComplet(e, unite))
                .toList();

        return new BienArborescenceResponse(uniteDto, parentsDto, sousBiensDto);
    }

    // ========================================================================
    // GESTION DES DÉTAILS
    // ========================================================================

    private void upsertDetailsDirect(BienEntity bien, TypeBien type, DetailsSpecifiquesRequest d) {
        if (type == null || d == null)
            return;

        switch (type) {
            case RESIDENTIEL -> upsertDetailsResidentielEtParking(bien, d);

            case COMMERCE -> {
                var existing = detailsCommerceRepository.findById(bien.getId()).orElse(null);
                if (existing != null) {
                    bienDetailsMapper.updateDetailsCommerceEntity(d, existing);
                    detailsCommerceRepository.save(existing);
                } else {
                    var created = bienDetailsMapper.toDetailsCommerceEntity(d, bien);
                    if (created != null)
                        detailsCommerceRepository.save(created);
                }
            }

            case BUREAU -> {
                var existing = detailsBureauRepository.findById(bien.getId()).orElse(null);
                if (existing != null) {
                    bienDetailsMapper.updateDetailsBureauEntity(d, existing);
                    detailsBureauRepository.save(existing);
                } else {
                    var created = bienDetailsMapper.toDetailsBureauEntity(d, bien);
                    if (created != null)
                        detailsBureauRepository.save(created);
                }
            }

            case PARKING -> {
                var existing = detailsParkingRepository.findById(bien.getId()).orElse(null);
                if (existing != null) {
                    bienDetailsMapper.updateDetailsParkingEntity(d, existing);
                    detailsParkingRepository.save(existing);
                } else {
                    var created = bienDetailsMapper.toDetailsParkingEntity(d, bien);
                    if (created != null)
                        detailsParkingRepository.save(created);
                }
            }
        }

        // Colocation (parties communes) - indépendant du type, stocké sur le bâtiment
        // parent
        if (bienDetailsMapper.hasColocationInfo(d)) {
            var existingColoc = detailsColocationRepository.findById(bien.getId()).orElse(null);
            if (existingColoc != null) {
                bienDetailsMapper.updateDetailsColocationEntity(d, existingColoc);
                detailsColocationRepository.save(existingColoc);
            } else {
                var createdColoc = bienDetailsMapper.toDetailsColocationEntity(d, bien);
                if (createdColoc != null)
                    detailsColocationRepository.save(createdColoc);
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
            if (created != null)
                detailsResidentielRepository.save(created);
        }

        var existingPark = detailsParkingRepository.findById(bien.getId()).orElse(null);
        if (bienDetailsMapper.hasParkingInfo(d)) {
            if (existingPark != null) {
                bienDetailsMapper.updateDetailsParkingEntity(d, existingPark);
                detailsParkingRepository.save(existingPark);
            } else {
                var createdPark = bienDetailsMapper.toDetailsParkingEntity(d, bien);
                if (createdPark != null)
                    detailsParkingRepository.save(createdPark);
            }
        } else {
            if (existingPark != null)
                detailsParkingRepository.delete(existingPark);
        }
    }

    // ========================================================================
    // GESTION PROPRIÉTAIRE
    // ========================================================================

    private void upsertLienProprietaire(BienEntity bien,
            BienOwnershipRequest ownership,
            Long currentUserId) {
        if (ownership == null || ownership.mode() == null)
            return;

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
    // HELPERS
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
            // Mode personnel : vérifier si l'utilisateur est propriétaire
            boolean isOwner = proprietaireBienRepository
                    .existsByBienIdAndProprietaireUtilisateurIdAndEnabledTrue(bienId, currentUserId);
            if (!isOwner)
                throw new ForbiddenException("Accès refusé (Personnel).");
            return;
        }

        if (contexte == TravailContext.AGENCE) {
            // Mode agence : utiliser le RBAC
            Long agenceId = bien.getAgenceId();
            if (agenceId == null)
                throw new ForbiddenException("Ce bien n'est lié à aucune agence.");

            // Vérifier la permission BIEN_VIEW_DETAILS pour ce bien spécifique
            boolean hasAccess = authorizationService.hasPermission(
                    currentUserId,
                    Permission.BIEN_VIEW_DETAILS,
                    contexte,
                    bienId);

            if (!hasAccess) {
                throw new ForbiddenException("Accès refusé (Agence). Vous n'avez pas les permissions nécessaires.");
            }
            return;
        }

        throw new ForbiddenException("Contexte invalide.");
    }

    /**
     * Propage l'adresse d'un parent à tous ses enfants.
     */
    private void propagateAddressToChildren(BienEntity parent) {
        List<BienEntity> enfants = bienRepository.findByParentBienIdAndEnabledTrue(parent.getId());
        for (BienEntity enfant : enfants) {
            enfant.setRue(parent.getRue());
            enfant.setNumero(parent.getNumero());
            enfant.setCodePostal(parent.getCodePostal());
            enfant.setVille(parent.getVille());
            enfant.setCommune(parent.getCommune());
            enfant.setPays(parent.getPays());
            enfant.setLatitude(parent.getLatitude());
            enfant.setLongitude(parent.getLongitude());
            // Note: on ne touche pas à boiteUnite qui est propre à chaque enfant
            bienRepository.save(enfant);
        }
    }

    // ========================================================================
    // GÉNÉRATION NOM_REFERENCE
    // ========================================================================

    /**
     * Génère automatiquement le nomReference avec le format:
     * {TYPE_COURT}-{ANNEE}-{SEQUENCE}/{SOUS_TYPE_COURT}-{IDENTIFIANT}
     * Ex: RES-2024-042/APT-3A, COM-2024-015/HOR-RDC, PRK-2024-008/GAR-12
     */
    private String generateNomReference(TypeBien typeBien, SousType sousType, String identifiant) {
        int annee = Year.now().getValue();
        String typeCode = getTypeCode(typeBien);
        String sousTypeCode = getSousTypeCode(sousType);

        // Compte les biens de ce type créés cette année pour la séquence
        Long count = bienRepository.countByTypeBienAndYear(typeBien.name(), annee);
        String sequence = String.format("%03d", count + 1);

        // Identifiant: boîte/étage ou "001" par défaut
        String ident = (identifiant != null && !identifiant.isBlank())
                ? identifiant.toUpperCase().replaceAll("[^A-Z0-9]", "")
                : sequence;

        return String.format("%s-%d-%s/%s-%s", typeCode, annee, sequence, sousTypeCode, ident);
    }

    private String getTypeCode(TypeBien type) {
        if (type == null)
            return "UNK";
        return switch (type) {
            case RESIDENTIEL -> "RES";
            case COMMERCE -> "COM";
            case BUREAU -> "BUR";
            case PARKING -> "PRK";
        };
    }

    private String getSousTypeCode(SousType sousType) {
        if (sousType == null)
            return "UNK";
        return switch (sousType) {
            // Résidentiel
            case MAISON -> "MAS";
            case APPARTEMENT -> "APT";
            case STUDIO -> "STU";
            case KOT -> "KOT";
            case LOFT -> "LOF";
            case CHAMBRE -> "CHB";
            case DUPLEX -> "DUP";
            case TRIPLEX -> "TRP";
            case PENTHOUSE -> "PEN";
            // Immeuble
            case IMMEUBLE_DE_RAPPORT -> "IMM";
            case RESIDENCE -> "RSD";
            // Commerce
            case COMMERCE_DE_DETAIL -> "CDT";
            case HORECA -> "HOR";
            case ENTREPOT -> "ENT";
            // Bureau
            case BUREAU_INDIVIDUEL -> "BIN";
            case OPEN_SPACE -> "OPS";
            case COWORKING -> "COW";
            // Parking
            case GARAGE_FERME -> "GAR";
            case EMPLACEMENT_EXTERIEUR -> "EXT";
            case EMPLACEMENT_INTERIEUR -> "INT";
        };
    }

    private BienResponse mapToBienComplet(BienEntity unite, BienEntity parent) {
        Long id = unite.getId();
        var res = detailsResidentielRepository.findById(id).orElse(null);
        var com = detailsCommerceRepository.findById(id).orElse(null);
        var bur = detailsBureauRepository.findById(id).orElse(null);
        var park = detailsParkingRepository.findById(id).orElse(null);
        var coloc = detailsColocationRepository.findById(id).orElse(null);

        var propEnfant = proprietaireBienRepository
                .findFirstByBienIdAndEnabledTrueOrderByIdAsc(id)
                .orElse(null);

        ProprietaireBienEntity propParent = null;
        if (parent != null) {
            propParent = proprietaireBienRepository
                    .findFirstByBienIdAndEnabledTrueOrderByIdAsc(parent.getId())
                    .orElse(null);
        }

        return bienResponseMapper.toBienComplet(unite, parent, res, com, bur, park, coloc, propEnfant, propParent);
    }

    // ========================================================================
    // SUPPRESSION (Soft Delete)
    // ========================================================================

    @Override
    @Transactional
    public int supprimerBien(Long bienId, Long currentUserId, TravailContext contexte) {
        BienEntity bien = bienRepository.findById(bienId)
                .orElseThrow(() -> new NotFoundException("Bien introuvable : " + bienId));

        checkAccessToBien(bien, currentUserId, contexte);

        int count = 0;

        // Si c'est un parent (pas une unité locative), désactiver aussi tous les
        // enfants
        if (!bien.isEstUniteLocative()) {
            List<BienEntity> enfants = bienRepository.findByParentBienIdAndEnabledTrue(bienId);
            for (BienEntity enfant : enfants) {
                enfant.setEnabled(false);
                bienRepository.save(enfant);
                count++;
            }
        }

        // Désactiver le bien lui-même
        bien.setEnabled(false);
        bienRepository.save(bien);
        count++;

        return count;
    }
}
