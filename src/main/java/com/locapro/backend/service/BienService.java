package com.locapro.backend.service;

import com.locapro.backend.domain.context.TravailContext;
import com.locapro.backend.dto.bien.BienCreationRequest;
import com.locapro.backend.dto.bien.BienResponse;
import com.locapro.backend.dto.bien.BienUpdateRequest;
import com.locapro.backend.dto.bien.BienArborescenceResponse;
import com.locapro.backend.entity.BienEntity;

import java.util.List;

public interface BienService {

    /**
     * Crée une structure complète (arbre récursif) de biens en une seule transaction.
     * Remplace les anciens creerBien / creerColocation.
     */
    BienResponse creerStructureComplete(BienCreationRequest request, Long currentUserId, TravailContext contexte);

    BienEntity getBienById(Long id);

    List<BienResponse> listerBiensAgence(Long currentUserId);

    List<BienResponse> listerBiensPerso(Long currentUserId);

    List<BienResponse> listerBatimentsAgence(Long currentUserId);

    List<BienResponse> listerBatimentsPerso(Long currentUserId);

    BienArborescenceResponse getBienComplet(Long bienId, Long currentUserId, TravailContext contexte);

    BienResponse mettreAJourBien(Long bienId, BienUpdateRequest request, Long currentUserId, TravailContext contexte);

    /**
     * Soft-delete d'un bien (enabled = false).
     * Si c'est un parent, désactive aussi tous les enfants.
     * Retourne le nombre de biens désactivés.
     */
    int supprimerBien(Long bienId, Long currentUserId, TravailContext contexte);
}
