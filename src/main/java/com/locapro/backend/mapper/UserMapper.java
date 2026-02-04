package com.locapro.backend.mapper;

import com.locapro.backend.dto.user.UserResponse;
import com.locapro.backend.entity.UtilisateurEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // Cas 1 : Utilisé pour le Register (on ne connait pas encore l'entreprise)
    public UserResponse toUserResponse(UtilisateurEntity entity) {
        // On appelle la méthode du dessous avec null
        return toUserResponseWithEntreprise(entity, null);
    }

    // Cas 2 : Utilisé pour le Login (AuthService) avec l'ID entreprise
    public UserResponse toUserResponseWithEntreprise(UtilisateurEntity entity, Long entrepriseId) {
        if (entity == null) {
            return null;
        }

        return new UserResponse(
                entity.getId(),
                entity.getPrenom(),
                entity.getNom(),
                entity.getEmail(),
                entity.getDateNaissance(),
                entity.getTelephone(),
                entity.getNumeroIpi(),
                Boolean.TRUE.equals(entity.isEmailVerified()),
                entity.getRue(),
                entity.getNumero(),
                entity.getBoite(),
                entity.getCodePostal(),
                entity.getVille(),
                entity.getCommune(),
                entity.getPays(),
                entrepriseId // 👈 On ajoute juste ça à la fin !
        );
    }
}