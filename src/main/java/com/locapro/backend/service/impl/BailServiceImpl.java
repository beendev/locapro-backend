package com.locapro.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locapro.backend.dto.bail.BailConflitResponse;
import com.locapro.backend.dto.bail.BailResponse;
import com.locapro.backend.dto.bail.CreateBailRequest;
import com.locapro.backend.dto.bail.UpdateBailRequest;
import com.locapro.backend.entity.*;
import com.locapro.backend.exception.BadRequestException;
import com.locapro.backend.exception.ConflictException;
import com.locapro.backend.exception.NotFoundException;
import com.locapro.backend.mapper.BailMapper;
import com.locapro.backend.repository.*;
import com.locapro.backend.service.BailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class BailServiceImpl implements BailService {

    private final BailRepository bailRepository;
    private final PeriodeBailRepository periodeBailRepository;
    private final BienRepository bienRepository;
    private final ModeleBailRepository modeleBailRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ObjectMapper objectMapper;
    private final LocataireRepository locataireRepository;
    private final BailLocataireRepository bailLocataireRepository;

    public BailServiceImpl(
            BailRepository bailRepository,
            PeriodeBailRepository periodeBailRepository,
            BienRepository bienRepository,
            ModeleBailRepository modeleBailRepository,
            UtilisateurRepository utilisateurRepository,
            ObjectMapper objectMapper, LocataireRepository locataireRepository, BailLocataireRepository bailLocataireRepository
    ) {
        this.bailRepository = bailRepository;
        this.periodeBailRepository = periodeBailRepository;
        this.bienRepository = bienRepository;
        this.modeleBailRepository = modeleBailRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.objectMapper = objectMapper;
        this.locataireRepository = locataireRepository;
        this.bailLocataireRepository = bailLocataireRepository;
    }

    @Override
    @Transactional
    public BailResponse creerBail(Long currentUserId, CreateBailRequest request) {

        // --- 0. VALIDATION TECHNIQUE & JURIDIQUE ---
        // Avant tout traitement, on vérifie que le dossier est complet pour un bail valide
        validerDonneesObligatoires(request);

        // --- 1. INTELLIGENCE MÉTIER : Calcul des dates ---
        LocalDate dateFinEffective = request.dateFin();

        // Calcul automatique fin de bail 9 ans si non précisé
        if (dateFinEffective == null && "CLASSIQUE_9ANS".equals(request.typeContrat().name())) {
            dateFinEffective = request.dateDebut().plusYears(9).minusDays(1);
        }

        // --- 2. VALIDATION DISPONIBILITÉ (Business Logic) ---
        verifierDisponibiliteBien(request.bienId(), request.dateDebut(), dateFinEffective);

        // --- 3. VALIDATION D'EXISTENCE (DB Checks) ---
        utilisateurRepository.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("Utilisateur (Agent) introuvable ID : " + currentUserId));

        BienEntity bien = bienRepository.findById(request.bienId())
                .orElseThrow(() -> new NotFoundException("Bien introuvable ID : " + request.bienId()));

        Long agenceIdContext = bien.getAgenceId();

        ModeleBailEntity modele = modeleBailRepository
                .findFirstByRegionBailAndTypeContratAndLangueAndActifBoolTrue(
                        request.region(),
                        request.typeContrat(),    // <--- On utilise l'Enum, pas la String typeDocument
                        request.langueContrat()   // <--- On ajoute la langue qui manquait
                )
                .orElseThrow(() -> new NotFoundException(
                        "Modèle introuvable pour : " + request.region() + " / " + request.typeContrat() + " / " + request.langueContrat()
                ));

        // --- 4. SÉRIALISATION JSON ---
        String reponsesBailJson;
        try {
            reponsesBailJson = objectMapper.writeValueAsString(request.reponseFormulaire());
        } catch (JsonProcessingException e) {
            throw new BadRequestException("JSON invalide dans 'reponseFormulaire'", e);
        }

        // --- 5. PERSISTANCE ---
        BailEntity bailToSave = BailMapper.toBailEntity(
                request, bien, modele, currentUserId, agenceIdContext, reponsesBailJson
        );
        bailToSave.setDateFin(dateFinEffective); // On force la date calculée

        BailEntity savedBail = bailRepository.save(bailToSave);

        PeriodeBailEntity periodeInitiale = BailMapper.toInitialPeriodeEntity(request, savedBail);
        periodeInitiale.setDateFin(dateFinEffective); // On force la date calculée

        PeriodeBailEntity savedPeriode = periodeBailRepository.save(periodeInitiale);

        return BailMapper.toResponse(savedBail, savedPeriode);
    }

    @Override
    @Transactional
    public BailResponse updateBail(Long bailId, UpdateBailRequest request) {
        // 1. Récupération
        BailEntity bail = bailRepository.findById(bailId)
                .orElseThrow(() -> new NotFoundException("Bail introuvable ID : " + bailId));

        // 2. Mise à jour des champs simples (si non null)
        if (request.nomBail() != null) bail.setNomBail(request.nomBail());
        if (request.region() != null) bail.setRegion(request.region().name());
        if (request.langueContrat() != null) bail.setLangueContrat(request.langueContrat().name());
        if (request.typeContrat() != null) bail.setTypeContrat(request.typeContrat().name());
        if (request.dateDebut() != null) bail.setDateDebut(request.dateDebut());
        if (request.dateFin() != null) bail.setDateFin(request.dateFin());
        if (request.loyerBase() != null) bail.setLoyerReference(request.loyerBase()); // Attention au mapping loyer
        if (request.ibanPaiement() != null) bail.setIbanPaiement(request.ibanPaiement());
        if (request.edlMode() != null) bail.setEdlMode(request.edlMode());
        if (request.edlExpertId() != null) bail.setEdlExpertId(request.edlExpertId());

        // 3. Mise à jour du JSON Wizard (Cœur du Draft)
        if (request.reponseFormulaire() != null && !request.reponseFormulaire().isEmpty()) {
            try {
                String json = objectMapper.writeValueAsString(request.reponseFormulaire());
                bail.setReponsesBail(json);
            } catch (JsonProcessingException e) {
                throw new BadRequestException("Erreur lors de la sérialisation du formulaire JSON", e);
            }
        }

        // 4. Logique de Finalisation (Seulement si on veut valider)
        // Si le client demande à passer en "EN_SIGNATURE" ou "VALIDE", là on vérifie tout.
        if (request.statut() != null && !request.statut().equals("BROUILLON")) {
            // On vérifie que tout est rempli avant de changer le statut
            validerBailComplet(bail);
            extraireEtSauvegarderLocataires(bail);
            bail.setStatut(request.statut());
        }

        // 5. Sauvegarde
        BailEntity updated = bailRepository.save(bail);

        // On récupère la période liée pour la réponse (simplifié ici)
        PeriodeBailEntity periode = periodeBailRepository.findFirstByBailId(bail.getId()).orElse(null);

        return BailMapper.toResponse(updated, periode);
    }

    @Override
    public BailResponse getBail(Long bailId) {
        BailEntity bail = bailRepository.findById(bailId)
                .orElseThrow(() -> new NotFoundException("Bail introuvable"));
        PeriodeBailEntity periode = periodeBailRepository.findFirstByBailId(bailId).orElse(null);
        return BailMapper.toResponse(bail, periode);
    }

    @Override
    @Transactional
    public void deleteBail(Long bailId) {
        if (!bailRepository.existsById(bailId)) {
            throw new NotFoundException("Bail introuvable");
        }
        // Attention: supprimer aussi les périodes associées (Cascade ou manuel)
        periodeBailRepository.deleteByBailId(bailId);
        bailRepository.deleteById(bailId);
    }

    @Override
    public BailConflitResponse verifierDisponibilite(Long bienId, LocalDate dateDebutPrevue) {
        // 1. Search for active or signing leases
        // The repository method findByBienIdAndStatutIn must exist in BailRepository
        List<BailEntity> bauxActifs = bailRepository.findByBienIdAndStatutIn(
                bienId,
                List.of("ACTIF", "EN_SIGNATURE")
        );

        if (bauxActifs.isEmpty()) {
            return new BailConflitResponse(false, null, "Le bien est libre.");
        }

        // 2. Check dates
        BailEntity conflit = bauxActifs.get(0);

        if (dateDebutPrevue != null && conflit.getDateFin() != null) {
            if (dateDebutPrevue.isAfter(conflit.getDateFin())) {
                return new BailConflitResponse(false, null, "Le bien sera libre à cette date.");
            }
        }

        String message = String.format("Un bail est déjà en cours (Fin prévue : %s)",
                conflit.getDateFin() != null ? conflit.getDateFin().toString() : "Indéterminée"
        );

        // Ensure BailMapper.toResponse(conflit) works and returns BailResponse
        return new BailConflitResponse(true, BailMapper.toResponse(conflit, null), message);
    }

    /**
     * Vérifie la présence des champs obligatoires pour la validité juridique du contrat.
     */
    private void validerDonneesObligatoires(CreateBailRequest req) {
        if (req.bienId() == null) {
            throw new BadRequestException("L'ID du bien est obligatoire.");
        }
        if (req.nomBail() == null || req.nomBail().isBlank()) {
            throw new BadRequestException("Le nom du bail est obligatoire (ex: 'Bail M. Dupont').");
        }
        if (req.dateDebut() == null) {
            throw new BadRequestException("La date de début de bail est obligatoire.");
        }
        if (req.loyerBase() == null || req.loyerBase().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Le loyer de base est obligatoire et doit être positif.");
        }
        if (req.ibanPaiement() == null || req.ibanPaiement().length() < 10) {
            throw new BadRequestException("L'IBAN de paiement est obligatoire pour le contrat.");
        }
        if (req.utilisateurResponsableId() == null) {
            throw new BadRequestException("Un utilisateur responsable doit être assigné.");
        }

        // Vérification basique du contenu du formulaire juridique
        Map<String, Object> form = req.reponseFormulaire();
        if (form == null || form.isEmpty()) {
            throw new BadRequestException("Les données du formulaire (parties, bien, clauses) sont manquantes.");
        }
        if (!form.containsKey("parties") || !form.containsKey("bien")) {
            throw new BadRequestException("Le formulaire doit contenir les sections 'parties' et 'bien'.");
        }
    }

    /**
     * Validation stricte appelée UNIQUEMENT quand on passe le statut à "EN_SIGNATURE" ou "VALIDE".
     * Si cette méthode passe, le bail est juridiquement complet et prêt à générer un PDF.
     */
    private void validerBailComplet(BailEntity bail) {
        // --- 1. Identification & Localisation ---
        if (bail.getNomBail() == null || bail.getNomBail().isBlank()) {
            throw new BadRequestException("Le nom du dossier (Nom du bail) est obligatoire pour finaliser.");
        }
        if (bail.getBienId() == null) {
            throw new BadRequestException("Aucun bien n'est associé à ce bail.");
        }
        if (bail.getRegion() == null || bail.getRegion().isBlank()) {
            throw new BadRequestException("La région (Bruxelles/Wallonie/Flandre) n'est pas définie.");
        }

        // --- 2. Dates & Type de contrat ---
        if (bail.getDateDebut() == null) {
            throw new BadRequestException("La date de prise d'effet (Date de début) est obligatoire.");
        }
        if (bail.getTypeContrat() == null || bail.getTypeContrat().isBlank()) {
            throw new BadRequestException("Le type de contrat (9 ans, court terme, etc.) doit être sélectionné.");
        }
        // Note: La date de fin peut être null pour un bail de 9 ans (calcul auto), donc on ne bloque pas si null.

        // --- 3. Financier (IBAN & Loyer) ---
        if (bail.getIbanPaiement() == null || bail.getIbanPaiement().length() < 10) {
            throw new BadRequestException("Un numéro de compte (IBAN) valide est obligatoire pour le contrat.");
        }
        // Le loyer est souvent stocké dans le JSON ou loyerReference, on vérifie a minima la référence
        // Si tu veux être strict sur le montant, il faudrait parser le JSON ici, mais l'IBAN est souvent le bloquant principal.

        // --- 4. Responsabilités ---
        if (bail.getUtilisateurResponsableId() == null) {
            throw new BadRequestException("Un agent responsable doit être assigné au dossier.");
        }

        // --- 5. Données du Formulaire (Wizard) ---
        if (bail.getReponsesBail() == null || bail.getReponsesBail().isBlank() || "{}".equals(bail.getReponsesBail())) {
            throw new BadRequestException("Le formulaire du bail est vide. Veuillez compléter les étapes du wizard.");
        }

        // --- 6. Langue ---
        if (bail.getLangueContrat() == null || bail.getLangueContrat().isBlank()) {
            throw new BadRequestException("La langue du contrat doit être définie.");
        }
    }

    private void verifierDisponibiliteBien(Long bienId, LocalDate dateDebut, LocalDate dateFin) {
        LocalDate dateFinCheck = (dateFin != null) ? dateFin : dateDebut.plusYears(99);
        if (periodeBailRepository.existsChevauchement(bienId, dateDebut, dateFinCheck)) {
            throw new ConflictException("Le bien est déjà loué sur cette période (chevauchement détecté).");
        }
    }

    // Pseudo-code logique à mettre dans ta méthode de validation

    private void extraireEtSauvegarderLocataires(BailEntity bail) {
        try {
            JsonNode rootNode = objectMapper.readTree(bail.getReponsesBail());
            JsonNode locatairesNode = rootNode.at("/parties/locataires"); // Chemin dans ton JSON

            if (locatairesNode.isArray()) {
                // 1. On nettoie les anciens liens pour ce bail (en cas de mise à jour)
                bailLocataireRepository.deleteByBailId(bail.getId());

                for (JsonNode locNode : locatairesNode) {
                    // Lecture des champs JSON (Attention aux nulls)
                    String nom = locNode.at("/details/nom").asText("");
                    String prenom = locNode.at("/details/prenom").asText("");
                    String email = locNode.at("/details/email").asText("");
                    String telephone = locNode.at("/details/telephone").asText("");
                    String niss = locNode.at("/details/registreNational").asText("");

                    // On saute si pas de nom (brouillon vide)
                    if (nom.isBlank()) continue;

                    // 2. Recherche ou Création (Dédoublonnage)
                    // On cherche d'abord par NISS, sinon par Email
                    LocataireEntity locataire = null;

                    if (!niss.isBlank()) {
                        locataire = locataireRepository
                                .findFirstByRegistreNationalAndGestionnaireId(niss, bail.getUtilisateurResponsableId())
                                .orElse(null);
                    }

                    if (locataire == null && !email.isBlank()) {
                        locataire = locataireRepository
                                .findFirstByEmailAndGestionnaireId(email, bail.getUtilisateurResponsableId())
                                .orElse(null);
                    }

                    if (locataire == null) {
                        locataire = new LocataireEntity();
                        locataire.setGestionnaireId(bail.getUtilisateurResponsableId());
                    }

                    // 3. Mise à jour des infos (Le bail le plus récent a raison)
                    locataire.setNom(nom);
                    locataire.setPrenom(prenom);
                    locataire.setEmail(email);
                    locataire.setTelephone(telephone);
                    locataire.setRegistreNational(niss);

                    // Mapper l'adresse si présente dans le JSON
                    locataire.setAdresseRue(locNode.at("/details/adresse").asText(""));
                    // ... autres champs ...

                    locataire = locataireRepository.save(locataire);

                    // 4. Création du lien
                    BailLocataireEntity lien = new BailLocataireEntity();
                    lien.setBailId(bail.getId());
                    lien.setLocataireId(locataire.getId());
                    lien.setRoleDansBail("PRENEUR"); // Par défaut

                    bailLocataireRepository.save(lien);
                }
            }
        } catch (Exception e) {
            // On log l'erreur mais on ne bloque pas la création du bail pour autant ?
            // Ou on lance une exception, selon ta préférence.
            System.err.println("Erreur lors de l'extraction des locataires : " + e.getMessage());
        }
    }
}