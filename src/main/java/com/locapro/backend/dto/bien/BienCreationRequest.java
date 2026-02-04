package com.locapro.backend.dto.bien;

import com.locapro.backend.domain.context.SousType;
import com.locapro.backend.domain.context.TypeBien;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO récursif « Lego » pour créer un arbre de biens en une seule requête.
 *
 * Exemples :
 *   - Cas simple (Immeuble + 1 Appart) : racine = immeuble, sousBiens[0] = appart.
 *   - Colocation (Immeuble → Appart → N Chambres) : 3 niveaux imbriqués.
 *   - Rattachement : existingParentId rempli → on crée les sousBiens sous ce parent existant.
 */
public record BienCreationRequest(

        // --- Rattachement à un parent existant (optionnel) ---
        Long existingParentId,

        // --- Identification ---
        @NotNull TypeBien typeBien,
        @NotNull String nomReferenceInterne,
        String libelleVisible,
        SousType sousType,
        String lotOuUnite,
        String numeroPorte,
        String boiteUnite,
        Boolean estUniteLocative,
        String description,
        BigDecimal revenuCadastral,

        // --- Adresse ---
        String rue,
        String numero,
        String boite,
        String codePostal,
        String ville,
        String commune,
        String pays,
        Double latitude,
        Double longitude,

        // --- Détails techniques (résidentiel / commerce / bureau / parking) ---
        DetailsSpecifiquesRequest details,

        // --- Propriétaire ---
        BienOwnershipRequest proprietaire,

        // --- Sous-biens récursifs ---
        @Valid List<BienCreationRequest> sousBiens

) {}
