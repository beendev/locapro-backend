package com.locapro.backend.controller;

import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.dto.portefeuille.PortefeuilleAjouterMembreRequest;
import com.locapro.backend.dto.portefeuille.PortefeuilleCreateRequest;
import com.locapro.backend.dto.portefeuille.PortefeuilleResponse;
import com.locapro.backend.security.JwtAuthFilter.UserPrincipal;
import com.locapro.backend.service.PortefeuilleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portefeuilles")
public class PortefeuilleController {

    private final PortefeuilleService portefeuilleService;

    public PortefeuilleController(PortefeuilleService portefeuilleService) {
        this.portefeuilleService = portefeuilleService;
    }

    @PostMapping("/agence")
    public ResponseEntity<PortefeuilleResponse> creerPortefeuilleAgence(
            Authentication authentication,
            @RequestBody @Valid PortefeuilleCreateRequest request
    ) {
        UserPrincipal p = (UserPrincipal) authentication.getPrincipal();
        var resp = portefeuilleService.creerPortefeuilleAgence(p.id(), request);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/agence")
    public ResponseEntity<List<PortefeuilleResponse>> listerPortefeuillesAgence(
            Authentication authentication
    ) {
        UserPrincipal p = (UserPrincipal) authentication.getPrincipal();
        var resp = portefeuilleService.listerPortefeuillesAgence(p.id());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{portefeuilleId}/quitter")
    public ResponseEntity<ApiMessageResponse> quitterPortefeuille(
            @PathVariable Long portefeuilleId,
            Authentication authentication
    ) {
        UserPrincipal p = (UserPrincipal) authentication.getPrincipal();
        var resp = portefeuilleService.quitterPortefeuille(portefeuilleId, p.id());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{portefeuilleId}/biens/{bienId}")
    public ResponseEntity<ApiMessageResponse> ajouterBienAuPortefeuille(
            @PathVariable Long portefeuilleId,
            @PathVariable Long bienId,
            Authentication authentication
    ) {
        UserPrincipal p = (UserPrincipal) authentication.getPrincipal();
        var resp = portefeuilleService.ajouterBienAuPortefeuille(portefeuilleId, bienId, p.id());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{portefeuilleId}/membres")
    public ResponseEntity<ApiMessageResponse> ajouterMembreAuPortefeuille(
            @PathVariable Long portefeuilleId,
            @RequestBody @Valid PortefeuilleAjouterMembreRequest request,
            Authentication authentication
    ) {
        UserPrincipal p = (UserPrincipal) authentication.getPrincipal();
        var resp = portefeuilleService.ajouterMembreAuPortefeuille(portefeuilleId, request, p.id());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{portefeuilleId}/membres/{utilisateurId}/retirer")
    public ResponseEntity<ApiMessageResponse> retirerMembreDuPortefeuille(
            @PathVariable Long portefeuilleId,
            @PathVariable Long utilisateurId,
            Authentication authentication
    ) {
        UserPrincipal p = (UserPrincipal) authentication.getPrincipal();
        var resp = portefeuilleService.retirerMembreDuPortefeuille(portefeuilleId, utilisateurId, p.id());
        return ResponseEntity.ok(resp);
    }
}
