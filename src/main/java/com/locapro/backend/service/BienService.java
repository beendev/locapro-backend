package com.locapro.backend.service;

import com.locapro.backend.domain.context.TravailContext;
import com.locapro.backend.dto.bien.BienCreationRequest;
import com.locapro.backend.dto.bien.BienResponse;
import com.locapro.backend.dto.bien.BienUpdateRequest;
import com.locapro.backend.dto.bien.ColocationCreationRequest;
import com.locapro.backend.dto.bien.BienArborescenceResponse;

import com.locapro.backend.entity.BienEntity;

import java.util.List;

public interface BienService {

    /**
     * Crée un bien (et ses détails) pour l'utilisateur courant,
     * en tenant compte du contexte (PROPRIETAIRE / AGENCE).
     *
     * @param request        données du formulaire de création
     * @param currentUserId  id de l'utilisateur connecté
     * @param contexte       valeur du header X-Context (par ex. "PROPRIETAIRE" ou "AGENCE"), peut être null
     */
    BienResponse creerBien(BienCreationRequest request, Long currentUserId, TravailContext contexte);

    List<BienResponse> creerColocation(ColocationCreationRequest request, Long currentUserId, TravailContext contexte);
    BienEntity getBienById(Long id);
    List<BienResponse> listerBiensAgence(Long currentUserId);
    BienArborescenceResponse getBienComplet(Long bienId, Long currentUserId, TravailContext contexte);

    List<BienResponse> listerBiensPerso(Long currentUserId);

    BienResponse mettreAJourBien(Long bienId, BienUpdateRequest request, Long currentUserId, TravailContext contexte);
}
