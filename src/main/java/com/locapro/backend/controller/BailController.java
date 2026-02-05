package com.locapro.backend.controller;

import com.locapro.backend.dto.bail.*; // Assure-toi que BailConflitResponse est bien ici
import com.locapro.backend.dto.common.ApiErrorResponse;
import com.locapro.backend.security.JwtAuthFilter.UserPrincipal;
import com.locapro.backend.service.BailGenerationService;
import com.locapro.backend.service.BailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/baux")
@Tag(name = "Gestion des Baux", description = "CRUD complet et génération de contrats.")
public class BailController {

    private final BailService bailService;
    private final BailGenerationService bailGenerationService;

    public BailController(BailService bailService, BailGenerationService bailGenerationService) {
        this.bailService = bailService;
        this.bailGenerationService = bailGenerationService;
    }

    // --- 1. Vérification Avant Création (NOUVEAU) ---
    @GetMapping("/check-conflit")
    @Operation(summary = "Vérifier la disponibilité d'un bien", description = "Permet au frontend de savoir si un bail actif bloque la création d'un nouveau.")
    public ResponseEntity<BailConflitResponse> checkConflit(
            @RequestParam Long bienId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut
    ) {
        return ResponseEntity.ok(bailService.verifierDisponibilite(bienId, dateDebut));
    }

    // --- 2. Création (POST) ---
    @PostMapping
    @Operation(summary = "Créer un nouveau Bail", description = "Initialise un bail en statut BROUILLON.")
    public ResponseEntity<BailResponse> creerBail(
            @RequestBody @Valid CreateBailRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        BailResponse response = bailService.creerBail(currentUser.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- 3. Lecture Détail (GET) ---
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer un bail par son ID")
    public ResponseEntity<BailResponse> getBail(@PathVariable Long id) {
        return ResponseEntity.ok(bailService.getBail(id));
    }

    // --- 4. Mise à jour (PUT) ---
    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un bail existant", description = "Permet de modifier un brouillon ou de changer le statut.")
    public ResponseEntity<BailResponse> updateBail(
            @PathVariable Long id,
            @RequestBody @Valid UpdateBailRequest request
    ) {
        return ResponseEntity.ok(bailService.updateBail(id, request));
    }

    // --- 5. Suppression (DELETE) ---
    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un bail", description = "Supprime définitivement le bail et ses périodes associées.")
    public ResponseEntity<Void> deleteBail(@PathVariable Long id) {
        bailService.deleteBail(id);
        return ResponseEntity.noContent().build();
    }

    // --- 6. Téléchargement PDF/Word (GET) ---
    @GetMapping("/{id}/download")
    @Operation(summary = "Télécharger le contrat généré (Word)")
    public ResponseEntity<byte[]> downloadBailContract(@PathVariable Long id) {
        try {
            byte[] wordContent = bailGenerationService.genererBailPourId(id);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.wordprocessingml.document"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Bail_" + id + ".docx\"")
                    .body(wordContent);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}