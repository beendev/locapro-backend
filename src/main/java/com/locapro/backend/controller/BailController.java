package com.locapro.backend.controller;

import com.locapro.backend.dto.bail.BailResponse;
import com.locapro.backend.dto.bail.CreateBailRequest;
import com.locapro.backend.dto.common.ApiErrorResponse;
import com.locapro.backend.security.JwtAuthFilter.UserPrincipal;
import com.locapro.backend.service.BailGenerationService;
import com.locapro.backend.service.BailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/baux")
@Tag(name = "Gestion des Baux", description = "Création, édition et génération de contrats de bail.")
public class BailController {

    private final BailService bailService;
    private final BailGenerationService bailGenerationService;

    public BailController(BailService bailService, BailGenerationService bailGenerationService) {
        this.bailService = bailService;
        this.bailGenerationService = bailGenerationService;
    }

    // --- 1. Endpoint de Création (POST) ---
    @PostMapping
    @Operation(
            summary = "Créer un nouveau Bail (Brouillon)",
            description = """
            Crée un contrat de bail en base de données, calcule les dates de fin automatiquement (si 9 ans) et vérifie la disponibilité du bien.
            
            ### Règles Métier :
            1. **Disponibilité** : Le système vérifie qu'aucun autre bail ne chevauche les dates pour le Bien ID 96.
            2. **Modèle** : Un modèle de document actif doit exister pour la région (**BXL**) et la langue (FR).
            3. **État des lieux** : Si le mode est `EXPERT`, l'ID de l'expert doit être valide (ex: 1).
            """,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Données pour un Bail Résidentiel à Bruxelles (Bien ID 96)",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateBailRequest.class),
                            examples = @ExampleObject(
                                    name = "Bail 9 ans - Apt Bruxelles (ID 96)",
                                    value = """
                                    {
                                      "bienId": 99,
                                      "nomBail": "Bail Résidence Étoile - Martin / Dupont",
                                      "region": "BXL",
                                      "langueContrat": "FR",
                                      "typeContrat": "CLASSIQUE_9ANS",
                                      "typeDocument": "BAIL_RESIDENCE_PRINCIPALE",
                                      "dateDebut": "2024-05-01",
                                      "loyerBase": 1250.00,
                                      "provisionCharges": 150.00,
                                      "typeIndice": "SANTE",
                                      "moisIndiceBase": "2024-04",
                                      "jourEcheance": 1,
                                      "ibanPaiement": "BE12 3456 7890 1234",
                                      "utilisateurResponsableId": 1,
                                    
                                      "edlMode": "EXPERT",
                                      "edlExpertId": 1,
                                    
                                      "descriptionBienSnapshot": "Appartement 3 chambres, rénové, cuisine hyper-équipée.",
                                      "reponseFormulaire": {
                                        "bien": {
                                            "adresse": "Avenue de l'Astronomie 42, 1210 Bruxelles",
                                            "etage": "3",
                                            "type": "Appartement"
                                        },
                                        "parties": {
                                            "bailleurs": [
                                                {
                                                    "type": "PERSONNE_PHYSIQUE",
                                                    "nom": "Martin",
                                                    "prenom": "Philippe",
                                                    "email": "philippe.martin@email.com",
                                                    "adresse": "Chemin de la Campagne 8, 1300 Wavre"
                                                }
                                            ],
                                            "preneurs": [
                                                {
                                                    "type": "PERSONNE_PHYSIQUE",
                                                    "nom": "Dupont",
                                                    "prenom": "Jean",
                                                    "email": "jean.dupont@test.com",
                                                    "telephone": "0470/12.34.56"
                                                }
                                            ]
                                        },
                                        "dispositions": {
                                            "animaux": "AUTORISE_SOUS_RESERVE",
                                            "fumeur": false
                                        }
                                      }
                                    }
                                    """
                            )
                    )
            )
    )
    @ApiResponse(responseCode = "201", description = "Bail créé avec succès",
            content = @Content(schema = @Schema(implementation = BailResponse.class)))
    @ApiResponse(responseCode = "400", description = "Données invalides (ex: BienId manquant, JSON malformé)",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "404", description = "Bien, Agent ou Modèle de bail introuvable",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Conflit : Le bien est déjà loué sur cette période",
            content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
    public ResponseEntity<BailResponse> creerBail(
            @RequestBody @Valid CreateBailRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        BailResponse response = bailService.creerBail(currentUser.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // --- 2. Endpoint de Téléchargement (GET) ---
    @GetMapping("/{id}/download")
    @Operation(summary = "Télécharger le Bail (Word)", description = "Génère le document Word basé sur le modèle et les données du bail.")
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