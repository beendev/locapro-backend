package com.locapro.backend.dto.bien;

public record BienResponse(
        Long id,
        String nomReference,
        String typeBien,
        String sousType,
        String libelleUnite,
        String codePublic,
        boolean estUniteLocative,
        Long portefeuilleId,

        // Adresse
        String rue,
        String numero,
        String boiteAdresse,
        String boiteUnite,
        String codePostal,
        String ville,
        String commune,
        String pays,

        // 👇 AJOUTS ICI : Coordonnées GPS
        Double latitude,
        Double longitude,

        // 👇 AJOUTS ICI : Infos Admin
        String statut,
        String notes,

        // Parent
        Long parentId,
        String parentNomReference,
        String parentLibelle,

        // Détails
        DetailsResidentielResponse detailsResidentiel,
        DetailsCommerceResponse detailsCommerce,
        DetailsBureauResponse detailsBureau,
        DetailsParkingResponse detailsParking,

        // Propriétaires
        ProprietaireBienResponse proprietaire,
        ProprietaireBienResponse proprietaireParent,

        String description,
        java.math.BigDecimal revenuCadastral,

        // Colocation (parties communes)
        DetailsColocationResponse detailsColocation
) {}