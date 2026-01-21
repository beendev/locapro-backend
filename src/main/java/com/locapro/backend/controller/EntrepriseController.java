package com.locapro.backend.controller;

import com.locapro.backend.dto.agence.EntrepriseRequest;
import com.locapro.backend.dto.agence.EntrepriseResponse;
import com.locapro.backend.security.JwtAuthFilter.UserPrincipal;
import com.locapro.backend.service.EntrepriseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/entreprise")
@Tag(name = "Gestion de l'Entreprise", description = "Endpoints pour gérer les informations légales et coordonnées de l'entreprise du propriétaire")
public class EntrepriseController {

    private final EntrepriseService entrepriseService;

    public EntrepriseController(EntrepriseService entrepriseService) {
        this.entrepriseService = entrepriseService;
    }

    @GetMapping("/current")
    @Operation(summary = "Récupérer l'entreprise actuelle",
            description = "Retourne les informations de l'entreprise active liée au propriétaire connecté.")
    public EntrepriseResponse getCurrent(@AuthenticationPrincipal UserPrincipal user) {
        return entrepriseService.getCurrent(user.id());
    }

    @PostMapping
    @Operation(summary = "Créer une nouvelle version",
            description = "Désactive l'ancienne entreprise et en crée une nouvelle. Utile pour un changement majeur de raison sociale ou de TVA.")
    public EntrepriseResponse createNewVersion(
            @Valid @RequestBody EntrepriseRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return entrepriseService.createNewVersion(user.id(), request);
    }

    @PostMapping("/update")
    @Operation(summary = "Mettre à jour l'entreprise",
            description = "Modifie les champs de l'entreprise actuelle sans en créer une nouvelle (mise à jour partielle des infos).")
    public EntrepriseResponse updateCurrent(
            @RequestBody EntrepriseRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return entrepriseService.updateCurrent(user.id(), request);
    }
}