package com.locapro.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locapro.backend.dto.bail.BailResponse;
import com.locapro.backend.dto.bail.CreateBailRequest;
import com.locapro.backend.entity.BailEntity;
import com.locapro.backend.entity.BienEntity;
import com.locapro.backend.entity.ModeleBailEntity;
import com.locapro.backend.entity.PeriodeBailEntity;
import com.locapro.backend.exception.BadRequestException;
import com.locapro.backend.exception.ConflictException;
import com.locapro.backend.exception.NotFoundException;
import com.locapro.backend.mapper.BailMapper;
import com.locapro.backend.repository.BailRepository;
import com.locapro.backend.repository.BienRepository;
import com.locapro.backend.repository.ModeleBailRepository;
import com.locapro.backend.repository.PeriodeBailRepository;
import com.locapro.backend.repository.UtilisateurRepository;
import com.locapro.backend.service.BailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
public class BailServiceImpl implements BailService {

    private final BailRepository bailRepository;
    private final PeriodeBailRepository periodeBailRepository;
    private final BienRepository bienRepository;
    private final ModeleBailRepository modeleBailRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final ObjectMapper objectMapper;

    public BailServiceImpl(
            BailRepository bailRepository,
            PeriodeBailRepository periodeBailRepository,
            BienRepository bienRepository,
            ModeleBailRepository modeleBailRepository,
            UtilisateurRepository utilisateurRepository,
            ObjectMapper objectMapper
    ) {
        this.bailRepository = bailRepository;
        this.periodeBailRepository = periodeBailRepository;
        this.bienRepository = bienRepository;
        this.modeleBailRepository = modeleBailRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.objectMapper = objectMapper;
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
                .findFirstByRegionBailAndLangueAndTypeDocumentAndActifBoolIsTrueOrderByCreeLeDesc(
                        request.region(),
                        request.langueContrat(),
                        request.typeDocument()
                )
                .orElseThrow(() -> new NotFoundException(
                        "Modèle introuvable pour : " + request.region() + " / " + request.langueContrat()
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

    private void verifierDisponibiliteBien(Long bienId, LocalDate dateDebut, LocalDate dateFin) {
        LocalDate dateFinCheck = (dateFin != null) ? dateFin : dateDebut.plusYears(99);
        if (periodeBailRepository.existsChevauchement(bienId, dateDebut, dateFinCheck)) {
            throw new ConflictException("Le bien est déjà loué sur cette période (chevauchement détecté).");
        }
    }
}