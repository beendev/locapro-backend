package com.locapro.backend.controller;

import com.locapro.backend.domain.context.TravailContext;
import com.locapro.backend.dto.bien.*;
import com.locapro.backend.security.JwtAuthFilter.UserPrincipal;
import com.locapro.backend.service.BienService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/biens")
public class BienController {

    private final BienService bienService;

    public BienController(BienService bienService) {
        this.bienService = bienService;
    }

    // ========================================================================
    // Création d'une unité "classique"
    // ========================================================================

    @PostMapping
    public ResponseEntity<BienResponse> creerBien(
            @RequestBody BienCreationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestHeader("X-Travail-Context") TravailContext contexte
    ) {
        BienResponse bienCree = bienService.creerBien(
                request,
                currentUser.id(),
                contexte
        );

        // Pattern REST : 201 + header Location
        URI location = URI.create("/biens/" + bienCree.id());

        return ResponseEntity
                .created(location)   // HTTP 201 Created
                .body(bienCree);     // BienResponse complet
    }

    // ========================================================================
    // Création d'une colocation (parent + appart + N chambres)
    // ========================================================================

    @PostMapping("/colocation")
    public ResponseEntity<List<BienResponse>> creerColocation(
            @RequestBody ColocationCreationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestHeader("X-Travail-Context") TravailContext contexte
    ) {
        List<BienResponse> chambresCreees = bienService.creerColocation(
                request,
                currentUser.id(),
                contexte
        );

        // Plusieurs ressources créées -> on renvoie 201, sans Location unique
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(chambresCreees);
    }

    // ========================================================================
    // Mise à jour d'un bien (unité locative)
    // ========================================================================

    @PostMapping("/{id}/update")
    public ResponseEntity<BienResponse> mettreAJourBien(
            @PathVariable("id") Long bienId,
            @RequestBody BienUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal utilisateur,
            @RequestHeader("X-Travail-Context") TravailContext contexte
    ) {
        BienResponse bienMisAJour = bienService.mettreAJourBien(
                bienId,
                request,
                utilisateur.id(),
                contexte
        );

        // Pattern : update réussi -> 200 OK + ressource modifiée
        return ResponseEntity.ok(bienMisAJour);
    }

    // ========================================================================
    // Listes
    // ========================================================================

    @GetMapping("/agence")
    public ResponseEntity<List<BienResponse>> getBiensAgence(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<BienResponse> biens = bienService.listerBiensAgence(currentUser.id());
        return ResponseEntity.ok(biens);
    }

    @GetMapping("/perso")
    public ResponseEntity<List<BienResponse>> getBiensPerso(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<BienResponse> biens = bienService.listerBiensPerso(currentUser.id());
        return ResponseEntity.ok(biens);
    }

    @GetMapping("/{id}/complet")
    public BienArborescenceResponse getBienComplet(
            @PathVariable("id") Long bienId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestHeader("X-Travail-Context") TravailContext contexte
    ) {
        return bienService.getBienComplet(
                bienId,
                currentUser.id(),
                contexte
        );
    }
}
