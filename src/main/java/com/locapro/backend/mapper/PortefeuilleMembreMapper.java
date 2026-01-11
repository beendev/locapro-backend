package com.locapro.backend.mapper;

import com.locapro.backend.entity.PortefeuilleEntity;
import com.locapro.backend.entity.PortefeuilleMembreEntity;
import com.locapro.backend.entity.UtilisateurEntity;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class PortefeuilleMembreMapper {

    public PortefeuilleMembreEntity asGestionnaire(PortefeuilleEntity p,
                                                   UtilisateurEntity user,
                                                   OffsetDateTime now) {
        PortefeuilleMembreEntity m = new PortefeuilleMembreEntity();
        m.setPortefeuille(p);
        m.setUtilisateur(user);
        m.setRoleDansPortefeuille("ADMIN");
        m.setJoinedAt(now);
        m.setEnabled(true);
        return m;
    }

    /**
     * Utilisé quand on ajoute un membre à un portefeuille :
     * si le membre existe déjà, on réactive + met à jour le rôle.
     */
    public void reactiverOuMettreAJour(PortefeuilleMembreEntity membre,
                                       String role,
                                       OffsetDateTime now) {
        // Réactiver si nécessaire
        if (!membre.isEnabled()) {
            membre.setEnabled(true);
            membre.setJoinedAt(now);
        }
        // Mettre à jour le rôle (tu peux adapter si tu veux garder l’ancien)
        if (role != null && !role.isBlank()) {
            membre.setRoleDansPortefeuille(role);
        }
    }

    /**
     * Création d’un nouveau membre avec un rôle donné.
     */
    public PortefeuilleMembreEntity createMembre(PortefeuilleEntity portefeuille,
                                                 UtilisateurEntity utilisateur,
                                                 String role,
                                                 OffsetDateTime now) {
        PortefeuilleMembreEntity membre = new PortefeuilleMembreEntity();
        membre.setPortefeuille(portefeuille);
        membre.setUtilisateur(utilisateur);
        membre.setRoleDansPortefeuille(
                (role == null || role.isBlank()) ? "GESTIONNAIRE" : role
        );
        membre.setJoinedAt(now);
        membre.setEnabled(true);
        return membre;
    }
}
