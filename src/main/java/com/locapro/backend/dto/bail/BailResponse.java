package com.locapro.backend.dto.bail;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BailResponse(
        Long id,
        String nomBail,
        Long bienId,
        Long modeleBailId,
        String region,
        String langueContrat,
        LocalDate dateDebut,
        LocalDate dateFin,
        String statut,
        BigDecimal loyerBase,
        BigDecimal provisionCharges,
        Integer jourEcheance,
        String moisIndiceBase,
        String typeIndice,
        String ibanPaiement,
        BigDecimal loyerReference
) {}
