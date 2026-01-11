package com.locapro.backend.dto.bail;

import com.locapro.backend.domain.context.LangueContrat;
import com.locapro.backend.domain.context.RegionBail;
import com.locapro.backend.domain.context.TypeContratBail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record CreateBailRequest(

        Long bienId,

        String nomBail,
        RegionBail region,             // BXL/WAL/VLA
        LangueContrat langueContrat,   // FR/NL

        String typeDocument,           // ✅ pour modeles_bail.type_document
        TypeContratBail typeContrat,   // ✅ pour baux.type_contrat (CLASSIQUE_9ANS/COURTE_DUREE)

        LocalDate dateDebut,
        LocalDate dateFin,

        BigDecimal loyerBase,
        BigDecimal provisionCharges,
        Integer jourEcheance,
        String moisIndiceBase,
        String typeIndice,

        String ibanPaiement,
        BigDecimal loyerReference,

        String descriptionBienSnapshot,
        String descriptionCommunesSnapshot,

        String edlMode,
        Long edlExpertId,

        Long portefeuilleId,
        Long utilisateurResponsableId,

        Map<String, Object> reponseFormulaire
) {}
