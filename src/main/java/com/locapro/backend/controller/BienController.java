package com.locapro.backend.controller;

import com.locapro.backend.domain.context.TravailContext;
import com.locapro.backend.dto.bien.*;
import com.locapro.backend.dto.common.ApiErrorResponse;
import com.locapro.backend.security.JwtAuthFilter.UserPrincipal;
import com.locapro.backend.service.BienService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/biens")
@Tag(name = "Gestion Immobilière", description = "Création, modification et consultation des biens et colocations.")
public class BienController {

    private final BienService bienService;

    public BienController(BienService bienService) {
        this.bienService = bienService;
    }

    // ========================================================================
    // Création d'une unité "classique" (Appartement, Maison, Commerce...)
    // ========================================================================

    @PostMapping
    @Operation(
            summary = "Créer un Bien (Unité + Parent)",
            description = """
            Cette ressource permet de créer une structure complète : **Parent (Immeuble) + Unité (Appartement) + Détails + Propriétaire**.
            
            ### 1. Gestion du Contexte (Header X-Travail-Context)
            | Valeur | Description | Impact sur le Body |
            | :--- | :--- | :--- |
            | **PERSONNEL** | Gestion en nom propre. | Le bloc **proprietaire** doit être en mode **SELF**. |
            | **AGENCE** | Gestion pour un tiers. | Le bloc **proprietaire** doit être **PERSONNE_PHYSIQUE** ou **ENTREPRISE**. |
            
            ### 2. Structure du Body JSON
            L'objet racine attend les blocs suivants :
            * **parent** (Obligatoire) : L'enveloppe physique (Immeuble, Résidence...).
            * **proprietaireParent** (Optionnel) : Si l'immeuble appartient à une entité différente (ex: Holding/Syndic).
            * **bien** (Obligatoire) : L'unité louable (Appartement 1, Commerce A...).
            * **details** (Obligatoire) : Les caractéristiques techniques.
            * **proprietaire** (Obligatoire) : Le propriétaire de l'unité locative (Objet Unique).
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Exemple complet avec Parent et Enfant ayant des propriétaires différents.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BienCreationRequest.class)
                    ))
    )
    @ApiResponse(responseCode = "201", description = "Bien créé avec succès",
            content = @Content(schema = @Schema(implementation = BienResponse.class)))
    @ApiResponse(responseCode = "400", description = "Erreur de validation",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<BienResponse> creerBien(
            @RequestBody BienCreationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,

            @Parameter(in = ParameterIn.HEADER, description = "Contexte de travail (PERSONNEL ou AGENCE)", required = true, example = "PERSONNEL")
            @RequestHeader("X-Travail-Context") TravailContext contexte
    ) {
        BienResponse bienCree = bienService.creerBien(
                request,
                currentUser.id(),
                contexte
        );

        URI location = URI.create("/biens/" + bienCree.id());
        return ResponseEntity.created(location).body(bienCree);
    }

    // ========================================================================
    // Création d'une colocation (Kots, Chambres, Studios...)
    // ========================================================================

    @PostMapping("/colocation")
    @Operation(
            summary = "Créer une Colocation",
            description = """
            Crée une structure complexe à 3 niveaux en une seule transaction :
            1. **Un Parent** (Immeuble / Maison).
            2. **Un Appartement** (L'unité commune, ex: Cuisine/Salon) -> Non locatif.
            3. **Plusieurs Chambres** (Les unités locatives).
            
            ### Flexibilité des Chambres
            Pour chaque élément de la liste `chambres`, vous pouvez définir :
            * **sousType** : `CHAMBRE` (défaut), `KOT`, `STUDIO`...
            * **proprietaire** : Optionnel. S'il est absent, la chambre hérite du propriétaire global défini à la racine.
            """

    )
    @ApiResponse(responseCode = "201", description = "Colocation créée (retourne la liste des chambres)",
            content = @Content(schema = @Schema(implementation = BienResponse.class)))
    public ResponseEntity<List<BienResponse>> creerColocation(
            @RequestBody ColocationCreationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(in = ParameterIn.HEADER, required = true) @RequestHeader("X-Travail-Context") TravailContext contexte
    ) {
        List<BienResponse> chambresCreees = bienService.creerColocation(
                request,
                currentUser.id(),
                contexte
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(chambresCreees);
    }

    // ========================================================================
    // Mise à jour
    // ========================================================================

    @PostMapping("/{id}/update")
    @Operation(summary = "Mettre à jour un bien", description = "Met à jour les informations du bien, de son parent, ses détails techniques et son propriétaire.")
    @ApiResponse(responseCode = "200", description = "Mise à jour réussie",
            content = @Content(schema = @Schema(implementation = BienResponse.class)))
    @ApiResponse(responseCode = "403", description = "Accès refusé",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Bien introuvable",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<BienResponse> mettreAJourBien(
            @Parameter(description = "ID du bien (unité) à modifier") @PathVariable("id") Long bienId,
            @RequestBody BienUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal utilisateur,
            @Parameter(in = ParameterIn.HEADER, required = true) @RequestHeader("X-Travail-Context") TravailContext contexte
    ) {
        BienResponse bienMisAJour = bienService.mettreAJourBien(
                bienId,
                request,
                utilisateur.id(),
                contexte
        );
        return ResponseEntity.ok(bienMisAJour);
    }

    // ========================================================================
    // Listes & Lectures
    // ========================================================================

    @GetMapping("/agence")
    @Operation(summary = "Lister les biens (Agence)", description = "Récupère tous les biens gérés par l'agence liée à l'utilisateur connecté.")
    @ApiResponse(responseCode = "200", description = "Liste des biens récupérée")
    public ResponseEntity<List<BienResponse>> getBiensAgence(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<BienResponse> biens = bienService.listerBiensAgence(currentUser.id());
        return ResponseEntity.ok(biens);
    }

    @GetMapping("/perso")
    @Operation(summary = "Lister les biens (Personnel)", description = "Récupère tous les biens appartenant en propre à l'utilisateur connecté.")
    @ApiResponse(responseCode = "200", description = "Liste des biens récupérée")
    public ResponseEntity<List<BienResponse>> getBiensPerso(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        List<BienResponse> biens = bienService.listerBiensPerso(currentUser.id());
        return ResponseEntity.ok(biens);
    }

    @GetMapping("/{id}/complet")
    @Operation(summary = "Détail complet d'un bien", description = "Récupère l'unité locative ainsi que toute son arborescence parente (Immeuble, Résidence...) pour affichage complet.")
    @ApiResponse(responseCode = "200", description = "Détail récupéré avec succès")
    @ApiResponse(responseCode = "404", description = "Bien introuvable",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public BienArborescenceResponse getBienComplet(
            @PathVariable("id") Long bienId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(in = ParameterIn.HEADER, required = true) @RequestHeader("X-Travail-Context") TravailContext contexte
    ) {
        return bienService.getBienComplet(
                bienId,
                currentUser.id(),
                contexte
        );
    }
}