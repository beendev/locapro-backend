package com.locapro.backend.dto.bail;

import com.locapro.backend.domain.context.LangueContrat;
import com.locapro.backend.domain.context.RegionBail;
import com.locapro.backend.domain.context.TypeContratBail;
import com.locapro.backend.domain.context.StatutBail;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * DTO souple pour la mise à jour (Sauvegarde auto / Draft).
 * Aucun champ n'est obligatoire ici, on met à jour uniquement ce qui est envoyé.
 */
public record UpdateBailRequest(
        String nomBail,
        String statut, // Pour passer de BROUILLON à EN_SIGNATURE

        RegionBail region,
        LangueContrat langueContrat,
        String typeDocument,
        TypeContratBail typeContrat,

        LocalDate dateDebut,
        LocalDate dateFin,

        BigDecimal loyerBase,
        BigDecimal provisionCharges,
        Integer jourEcheance,

        String ibanPaiement,
        String descriptionBienSnapshot,

        String edlMode,
        Long edlExpertId,

        Long utilisateurResponsableId,

        // Le plus important pour le Wizard : le JSON partiel ou complet
        Map<String, Object> reponseFormulaire
) {}