package com.locapro.backend.service;

import com.locapro.backend.dto.bien.BienResponse;
import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.dto.portefeuille.PortefeuilleAjouterMembreRequest;
import com.locapro.backend.dto.portefeuille.PortefeuilleCreateRequest;
import com.locapro.backend.dto.portefeuille.PortefeuilleMembreResponse;
import com.locapro.backend.dto.portefeuille.PortefeuilleResponse;

import java.util.List;

public interface PortefeuilleService {


    PortefeuilleResponse creerPortefeuilleAgence(Long currentUserId,
                                                 PortefeuilleCreateRequest request);

    List<PortefeuilleResponse> listerPortefeuillesAgence(Long currentUserId);

    ApiMessageResponse quitterPortefeuille(Long portefeuilleId, Long currentUserId);


    ApiMessageResponse ajouterBienAuPortefeuille(Long portefeuilleId,
                                                 Long bienId,
                                                 Long currentUserId);


    ApiMessageResponse ajouterMembreAuPortefeuille(Long portefeuilleId,
                                                   PortefeuilleAjouterMembreRequest request,
                                                   Long currentUserId);


    ApiMessageResponse retirerMembreDuPortefeuille(Long portefeuilleId,
                                                   Long utilisateurId,
                                                   Long currentUserId);

    List<BienResponse> listerBiensDuPortefeuille(Long portefeuilleId, Long userId);
    List<BienResponse> listerBiensDisponibles(Long userId);

    List<PortefeuilleMembreResponse> listerMembres(Long portefeuilleId, Long currentUserId);

    List<PortefeuilleMembreResponse> rechercherCollegues(Long agenceId, String query);
}
