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
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/biens")
@Tag(name = "Gestion Immobilière", description = "Création, modification et consultation des biens.")
public class BienController {

    private final BienService bienService;

    public BienController(BienService bienService) {
        this.bienService = bienService;
    }

    // ========================================================================
    // Création d'une structure complète (arbre récursif)
    // ========================================================================

    @PostMapping("/structure")
    @Operation(
            summary = "Créer une structure de biens (Arbre récursif)",
            description = """
            Endpoint unique pour créer n'importe quelle structure immobilière en une seule requête :

            ### Cas d'usage
            | Structure | Description |
            | :--- | :--- |
            | **Simple** | Immeuble + 1 Appartement |
            | **Multi-unités** | Immeuble + N Appartements |
            | **Colocation** | Immeuble → Appart → N Chambres (3 niveaux) |
            | **Rattachement** | Ajout d'unités sous un parent existant via `existingParentId` |

            ### Principe récursif
            Chaque nœud du JSON peut contenir un tableau `sousBiens` qui reprend la même structure.
            Le serveur parcourt l'arbre : **Sauvegarde Parent → Récupère ID → Sauvegarde Enfants**.

            ### Héritage
            - **Adresse** : si un enfant ne spécifie pas d'adresse, il hérite de celle du parent.
            """
    )
    @ApiResponse(responseCode = "201", description = "Structure créée avec succès",
            content = @Content(schema = @Schema(implementation = BienResponse.class)))
    @ApiResponse(responseCode = "400", description = "Erreur de validation",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<BienResponse> creerStructure(
            @RequestBody @Valid BienCreationRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(in = ParameterIn.HEADER, description = "Contexte de travail", required = true, example = "PERSONNEL")
            @RequestHeader("X-Travail-Context") TravailContext contexte
    ) {
        BienResponse created = bienService.creerStructureComplete(
                request, currentUser.id(), contexte
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // ========================================================================
    // Mise à jour
    // ========================================================================

    @PostMapping("/{id}/update")
    @Operation(summary = "Mettre à jour un bien",
            description = "Met à jour les informations du bien, de son parent, ses détails techniques et son propriétaire.")
    @ApiResponse(responseCode = "200", description = "Mise à jour réussie",
            content = @Content(schema = @Schema(implementation = BienResponse.class)))
    @ApiResponse(responseCode = "403", description = "Accès refusé",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Bien introuvable",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<BienResponse> mettreAJourBien(
            @Parameter(description = "ID du bien à modifier") @PathVariable("id") Long bienId,
            @RequestBody BienUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal utilisateur,
            @Parameter(in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-Travail-Context") TravailContext contexte
    ) {
        BienResponse updated = bienService.mettreAJourBien(
                bienId, request, utilisateur.id(), contexte
        );
        return ResponseEntity.ok(updated);
    }

    // ========================================================================
    // Listes & Lectures
    // ========================================================================

    @GetMapping("/agence")
    @Operation(summary = "Lister les biens (Agence)",
            description = "Récupère tous les biens gérés par l'agence liée à l'utilisateur connecté.")
    public ResponseEntity<List<BienResponse>> getBiensAgence(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(bienService.listerBiensAgence(currentUser.id()));
    }

    @GetMapping("/perso")
    @Operation(summary = "Lister les biens (Personnel)",
            description = "Récupère tous les biens appartenant en propre à l'utilisateur connecté.")
    public ResponseEntity<List<BienResponse>> getBiensPerso(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(bienService.listerBiensPerso(currentUser.id()));
    }

    @GetMapping("/agence/batiments")
    @Operation(summary = "Lister les bâtiments (Agence)",
            description = "Récupère tous les bâtiments (parents) gérés par l'agence.")
    public ResponseEntity<List<BienResponse>> getBatimentsAgence(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(bienService.listerBatimentsAgence(currentUser.id()));
    }

    @GetMapping("/perso/batiments")
    @Operation(summary = "Lister les bâtiments (Personnel)",
            description = "Récupère tous les bâtiments (parents) appartenant à l'utilisateur.")
    public ResponseEntity<List<BienResponse>> getBatimentsPerso(
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        return ResponseEntity.ok(bienService.listerBatimentsPerso(currentUser.id()));
    }

    @GetMapping("/{id}/complet")
    @Operation(summary = "Détail complet d'un bien",
            description = "Récupère l'unité locative et toute son arborescence parente.")
    @ApiResponse(responseCode = "200", description = "Détail récupéré avec succès")
    @ApiResponse(responseCode = "404", description = "Bien introuvable",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public BienArborescenceResponse getBienComplet(
            @PathVariable("id") Long bienId,
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Parameter(in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-Travail-Context") TravailContext contexte
    ) {
        return bienService.getBienComplet(bienId, currentUser.id(), contexte);
    }

    // ========================================================================
    // Suppression (Soft Delete)
    // ========================================================================

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un bien (soft delete)",
            description = """
            Désactive un bien (enabled = false). Le bien n'est jamais réellement supprimé.

            - Si c'est une **unité** : seule l'unité est désactivée.
            - Si c'est un **parent (bâtiment)** : le parent ET tous ses enfants sont désactivés.

            Retourne le nombre de biens désactivés.
            """)
    @ApiResponse(responseCode = "200", description = "Suppression réussie")
    @ApiResponse(responseCode = "403", description = "Accès refusé",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Bien introuvable",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<java.util.Map<String, Object>> supprimerBien(
            @Parameter(description = "ID du bien à supprimer") @PathVariable("id") Long bienId,
            @AuthenticationPrincipal UserPrincipal utilisateur,
            @Parameter(in = ParameterIn.HEADER, required = true)
            @RequestHeader("X-Travail-Context") TravailContext contexte
    ) {
        int count = bienService.supprimerBien(bienId, utilisateur.id(), contexte);
        return ResponseEntity.ok(java.util.Map.of(
                "success", true,
                "message", count + " bien(s) désactivé(s)",
                "count", count
        ));
    }
}
