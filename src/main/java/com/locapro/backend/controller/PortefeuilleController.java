package com.locapro.backend.controller;

import com.locapro.backend.dto.bien.BienResponse;
import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.dto.portefeuille.PortefeuilleAjouterMembreRequest;
import com.locapro.backend.dto.portefeuille.PortefeuilleCreateRequest;
import com.locapro.backend.dto.portefeuille.PortefeuilleMembreResponse;
import com.locapro.backend.dto.portefeuille.PortefeuilleResponse;
import com.locapro.backend.security.JwtAuthFilter.UserPrincipal;
import com.locapro.backend.service.PortefeuilleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/portefeuilles")
@Tag(name = "Gestion des Portefeuilles", description = "Endpoints pour la gestion des portefeuilles immobiliers (Agences et Individuels)")
public class PortefeuilleController {

    private final PortefeuilleService portefeuilleService;

    public PortefeuilleController(PortefeuilleService portefeuilleService) {
        this.portefeuilleService = portefeuilleService;
    }

    @PostMapping("/agence")
    @Operation(summary = "Créer un portefeuille d'agence",
            description = "Permet à un Admin/Responsable d'agence de créer un nouveau portefeuille.")
    public ResponseEntity<PortefeuilleResponse> creerPortefeuilleAgence(
            Authentication authentication,
            @RequestBody @Valid PortefeuilleCreateRequest request
    ) {
        UserPrincipal p = (UserPrincipal) authentication.getPrincipal();
        var resp = portefeuilleService.creerPortefeuilleAgence(p.id(), request);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/agence")
    @Operation(summary = "Lister mes portefeuilles",
            description = "Récupère la liste de tous les portefeuilles dont l'utilisateur est membre actif.")
    public ResponseEntity<List<PortefeuilleResponse>> listerPortefeuillesAgence(
            Authentication authentication
    ) {
        UserPrincipal p = (UserPrincipal) authentication.getPrincipal();
        var resp = portefeuilleService.listerPortefeuillesAgence(p.id());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{portefeuilleId}/quitter")
    @Operation(summary = "Quitter un portefeuille",
            description = "Permet à l'utilisateur actuel de se retirer d'un portefeuille spécifique.")
    public ResponseEntity<ApiMessageResponse> quitterPortefeuille(
            @PathVariable Long portefeuilleId,
            Authentication authentication
    ) {
        UserPrincipal p = (UserPrincipal) authentication.getPrincipal();
        var resp = portefeuilleService.quitterPortefeuille(portefeuilleId, p.id());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/{portefeuilleId}/biens/{bienId}")
    @Operation(summary = "Ajouter un bien au portefeuille",
            description = "Associe un bien immobilier à un portefeuille (Nécessite d'être Gestionnaire du portefeuille).")
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
    @Operation(summary = "Ajouter un membre",
            description = "Ajoute un collaborateur au portefeuille (Nécessite d'être Gestionnaire).")
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
    @Operation(summary = "Retirer un membre",
            description = "Retire un collaborateur du portefeuille.")
    public ResponseEntity<ApiMessageResponse> retirerMembreDuPortefeuille(
            @PathVariable Long portefeuilleId,
            @PathVariable Long utilisateurId,
            Authentication authentication
    ) {
        UserPrincipal p = (UserPrincipal) authentication.getPrincipal();
        var resp = portefeuilleService.retirerMembreDuPortefeuille(portefeuilleId, utilisateurId, p.id());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{portefeuilleId}/biens")
    @Operation(summary = "Lister les biens d'un portefeuille", description = "Renvoie la liste légère (résumé) des biens contenus dans ce portefeuille.")
    public ResponseEntity<List<BienResponse>> listerBiensDuPortefeuille(
            @PathVariable Long portefeuilleId,
            Authentication auth
    ) {
        var p = (UserPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(portefeuilleService.listerBiensDuPortefeuille(portefeuilleId, p.id()));
    }

    @GetMapping("/biens-disponibles")
    @Operation(summary = "Lister les biens orphelins", description = "Renvoie la liste des biens de l'agence qui ne sont pas encore assignés à un portefeuille.")
    public ResponseEntity<List<BienResponse>> listerBiensDisponibles(Authentication auth) {
        var p = (UserPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(portefeuilleService.listerBiensDisponibles(p.id()));
    }

    @GetMapping("/{id}/membres")
    @Operation(summary = "Lister les membres", description = "Voir qui a accès à ce portefeuille.")
    public ResponseEntity<List<PortefeuilleMembreResponse>> listerMembres(
            @PathVariable Long id,
            Authentication auth
    ) {
        var p = (UserPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(portefeuilleService.listerMembres(id, p.id()));
    }

    @GetMapping("/agence/{agenceId}/collegues")
    @Operation(summary = "Rechercher des collègues", description = "Recherche des membres de l'agence par nom ou email.")
    public ResponseEntity<List<PortefeuilleMembreResponse>> rechercherCollegues(
            @PathVariable Long agenceId,
            @RequestParam("q") String query
    ) {
        // On appelle le service qui fait la requête LIKE %query%
        return ResponseEntity.ok(portefeuilleService.rechercherCollegues(agenceId, query));
    }
}