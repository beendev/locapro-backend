package com.locapro.backend.mapper;

import com.locapro.backend.dto.portefeuille.PortefeuilleCreateRequest;
import com.locapro.backend.dto.portefeuille.PortefeuilleResponse;
import com.locapro.backend.entity.AgenceEntity;
import com.locapro.backend.entity.PortefeuilleEntity;
import com.locapro.backend.entity.UtilisateurEntity;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class PortefeuilleMapper {

    /**
     * Construit un portefeuille d'agence à partir du DTO + agence + créateur.
     */
    public PortefeuilleEntity fromAgenceCreateRequest(PortefeuilleCreateRequest request,
                                                      AgenceEntity agence,
                                                      UtilisateurEntity createdBy,
                                                      OffsetDateTime now) {

        PortefeuilleEntity p = new PortefeuilleEntity();
        p.setNom(request.nom());
        p.setCode(request.code());
        p.setType("AGENCE");

        // si rien fourni -> "AGENCE"
        String typeGestion = request.typeGestion() != null
                ? request.typeGestion()
                : "AGENCE";
        p.setTypeGestion(typeGestion);

        p.setAgence(agence);
        p.setUtilisateur(null);     // portefeuille d'agence
        p.setActifBool(true);
        p.setEnabled(true);
        p.setCreatedAt(now);
        p.setUpdatedAt(now);
        p.setCreatedBy(createdBy);

        return p;
    }

    /**
     * Map entité -> DTO de réponse.
     * Adapte aux champs réels de ton PortefeuilleResponse.
     */
    public PortefeuilleResponse toResponse(PortefeuilleEntity p) {
        return new PortefeuilleResponse(
                p.getId(),
                p.getNom(),
                p.getCode(),
                p.getType(),
                p.getTypeGestion(),
                p.getAgence() != null ? p.getAgence().getId() : null,
                p.getUtilisateur() != null ? p.getUtilisateur().getId() : null,
                p.isActifBool()

        );
    }
}
