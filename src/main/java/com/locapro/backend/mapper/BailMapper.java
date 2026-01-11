package com.locapro.backend.mapper;

import com.locapro.backend.dto.bail.BailResponse;
import com.locapro.backend.dto.bail.CreateBailRequest;
import com.locapro.backend.entity.BailEntity;
import com.locapro.backend.entity.BienEntity;
import com.locapro.backend.entity.ModeleBailEntity;
import com.locapro.backend.entity.PeriodeBailEntity;

import java.time.OffsetDateTime;

public class BailMapper {

    private BailMapper() {
        // util class
    }

    /**
     * Création de l'entité Bail à partir du wizard
     * - Mapping uniquement (pas de logique métier)
     * - IDs only (pas d'objets User/Agence)
     */
    public static BailEntity toBailEntity(
            CreateBailRequest request,
            BienEntity bien,
            ModeleBailEntity modeleBail,
            Long currentUserId,
            Long agenceIdContext,
            String reponsesBailJson
    ) {
        BailEntity bail = new BailEntity();

        bail.setCreeLe(OffsetDateTime.now());
        bail.setMajLe(OffsetDateTime.now());
        bail.setEnabled(true);

        bail.setNomBail(request.nomBail());
        bail.setBienId(bien.getId());

        bail.setRegion(request.region() != null ? request.region().name() : null);
        bail.setLangueContrat(request.langueContrat() != null ? request.langueContrat().name() : null);

        bail.setDateDebut(request.dateDebut());
        bail.setDateFin(request.dateFin());
        bail.setStatut("BROUILLON");

        // ✅ type_contrat (baux)
        bail.setTypeContrat(request.typeContrat() != null ? request.typeContrat().name() : "CLASSIQUE_9ANS");

        // default V1
        bail.setSourceContrat("NUMERIQUE");


        // IDs
        bail.setAgentId(currentUserId);
        bail.setUtilisateurResponsableId(
                request.utilisateurResponsableId() != null ? request.utilisateurResponsableId() : currentUserId
        );
        bail.setGestionnaireUtilisateurId(currentUserId);
        bail.setAgenceId(agenceIdContext);

        bail.setIbanPaiement(request.ibanPaiement());
        bail.setLoyerReference(request.loyerReference());

        bail.setDescriptionBienSnapshot(request.descriptionBienSnapshot() != null ? request.descriptionBienSnapshot() : "");
        bail.setDescriptionCommunesSnapshot(request.descriptionCommunesSnapshot() != null ? request.descriptionCommunesSnapshot() : "");

        bail.setEdlMode(request.edlMode());
        bail.setEdlExpertId(request.edlExpertId());

        bail.setPortefeuilleId(request.portefeuilleId());

        // ✅ modele_bail_id
        bail.setModeleBailId(modeleBail.getId());

        // jsonb brut
        bail.setReponsesBail(reponsesBailJson);

        return bail;
    }

    /**
     * Période initiale du bail
     */
    public static PeriodeBailEntity toInitialPeriodeEntity(
            CreateBailRequest request,
            BailEntity bail
    ) {
        PeriodeBailEntity periode = new PeriodeBailEntity();

        periode.setBail(bail);
        periode.setDateDebut(request.dateDebut());
        periode.setDateFin(request.dateFin());
        periode.setLoyerBase(request.loyerBase());
        periode.setProvisionCharges(request.provisionCharges());
        periode.setJourEcheance(request.jourEcheance());
        periode.setMoisIndiceBase(request.moisIndiceBase());
        periode.setTypeIndice(request.typeIndice() != null ? request.typeIndice() : "SANTE");
        periode.setOrigine("INITIAL");
        periode.setEnabled(true);

        return periode;
    }

    /**
     * Entity -> DTO
     */
    public static BailResponse toResponse(
            BailEntity bail,
            PeriodeBailEntity periode
    ) {
        return new BailResponse(
                bail.getId(),
                bail.getNomBail(),
                bail.getBienId(),
                bail.getModeleBailId(),
                bail.getRegion(),
                bail.getLangueContrat(),
                bail.getDateDebut(),
                bail.getDateFin(),
                bail.getStatut(),
                periode.getLoyerBase(),
                periode.getProvisionCharges(),
                periode.getJourEcheance(),
                periode.getMoisIndiceBase(),
                periode.getTypeIndice(),
                bail.getIbanPaiement(),
                bail.getLoyerReference()
        );
    }
}
