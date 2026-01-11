package com.locapro.backend.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.locapro.backend.dto.bail.BailResponse;
import com.locapro.backend.dto.bail.CreateBailRequest;
import com.locapro.backend.entity.BailEntity;
import com.locapro.backend.entity.BienEntity;
import com.locapro.backend.entity.ModeleBailEntity;
import com.locapro.backend.entity.PeriodeBailEntity;
import com.locapro.backend.mapper.BailMapper;
import com.locapro.backend.repository.BailRepository;
import com.locapro.backend.repository.BienRepository;
import com.locapro.backend.repository.ModeleBailRepository;
import com.locapro.backend.repository.PeriodeBailRepository;
import com.locapro.backend.repository.UtilisateurRepository;
import com.locapro.backend.service.BailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // A) check user existe (facultatif mais ok)
        utilisateurRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable: " + currentUserId));

        // B) bien
        BienEntity bien = bienRepository.findById(request.bienId())
                .orElseThrow(() -> new IllegalArgumentException("Bien introuvable: " + request.bienId()));

        // C) agence context (ID only)
        Long agenceIdContext = bien.getAgenceId(); // ✅ adapte si ton getter s'appelle autrement

        // D) modèle actif via type_document
        ModeleBailEntity modele = modeleBailRepository
                .findFirstByRegionBailAndLangueAndTypeDocumentAndActifBoolIsTrueOrderByCreeLeDesc(
                        request.region(),
                        request.langueContrat(),
                        request.typeDocument()
                )
                .orElseThrow(() -> new IllegalArgumentException(
                        "Aucun modèle actif trouvé pour region=" + request.region()
                                + " langue=" + request.langueContrat()
                                + " typeDocument=" + request.typeDocument()
                ));

        // E) JSON brut (vrai JSON, pas toString() d’une Map)
        String reponsesBailJson;
        try {
            reponsesBailJson = objectMapper.writeValueAsString(request.reponseFormulaire());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Impossible de sérialiser reponseFormulaire en JSON", e);
        }

        // F) bail
        BailEntity bailToSave = BailMapper.toBailEntity(
                request,
                bien,
                modele,
                currentUserId,
                agenceIdContext,
                reponsesBailJson
        );

        BailEntity savedBail = bailRepository.save(bailToSave);

        // G) période initiale
        PeriodeBailEntity periodeInitiale = BailMapper.toInitialPeriodeEntity(request, savedBail);
        PeriodeBailEntity savedPeriode = periodeBailRepository.save(periodeInitiale);

        // H) response
        return BailMapper.toResponse(savedBail, savedPeriode);
    }

}
