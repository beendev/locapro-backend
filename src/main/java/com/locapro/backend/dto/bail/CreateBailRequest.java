package com.locapro.backend.dto.bail;

import com.locapro.backend.domain.context.DureeBail;
import com.locapro.backend.domain.context.LangueContrat;
import com.locapro.backend.domain.context.RegionBail;
import com.locapro.backend.domain.context.TypeContratBail;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

public record CreateBailRequest(

        @NotNull(message = "L'ID du bien est obligatoire")
        Long bienId,

        @NotBlank(message = "Le nom du bail est obligatoire")
        String nomBail,

        @NotNull(message = "La région est obligatoire")
        RegionBail region,

        @NotNull(message = "La langue du contrat est obligatoire")
        LangueContrat langueContrat,

        @NotNull(message = "Le type de contrat est obligatoire")
        TypeContratBail typeContrat,  // <--- Utilise le NOUVEL Enum (RESIDENCE_PRINCIPALE...)

        @NotNull(message = "La durée du contrat (catégorie) est obligatoire")
        DureeBail dureeBail,
        // <--- NOUVEAU CHAMP !


        @NotNull(message = "La date de début est obligatoire")
        LocalDate dateDebut,

        LocalDate dateFin, // Optionnel (calculé si null)

        @NotNull(message = "Le loyer de base est obligatoire")
        @Positive(message = "Le loyer doit être positif")
        BigDecimal loyerBase,

        @PositiveOrZero(message = "La provision pour charges ne peut pas être négative")
        BigDecimal provisionCharges,

        @Min(1) @Max(31)
        Integer jourEcheance,

        String moisIndiceBase, // Peut être null (calculé auto)
        String typeIndice,     // Default SANTE

        @NotBlank(message = "L'IBAN est obligatoire")
        @Size(min = 10, message = "Format IBAN invalide")
        String ibanPaiement,

        BigDecimal loyerReference,

        String descriptionBienSnapshot,
        String descriptionCommunesSnapshot,

        String edlMode,
        Long edlExpertId,

        Long portefeuilleId,

        @NotNull(message = "Un utilisateur responsable (Agent) est obligatoire")
        Long utilisateurResponsableId,

        @NotNull(message = "Le formulaire de réponses ne peut pas être null")
        @NotEmpty(message = "Le formulaire doit contenir des données")
        Map<String, Object> reponseFormulaire
) {}