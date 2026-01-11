// com.locapro.backend.controller.EntrepriseController
package com.locapro.backend.controller;

import com.locapro.backend.dto.agence.EntrepriseRequest;
import com.locapro.backend.dto.agence.EntrepriseResponse;
import com.locapro.backend.security.JwtAuthFilter.UserPrincipal;
import com.locapro.backend.service.EntrepriseService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/entreprise")
public class EntrepriseController {

    private final EntrepriseService entrepriseService;

    public EntrepriseController(EntrepriseService entrepriseService) {
        this.entrepriseService = entrepriseService;
    }

    @GetMapping("/current")
    public EntrepriseResponse getCurrent(@AuthenticationPrincipal UserPrincipal user) {
        return entrepriseService.getCurrent(user.id());
    }

    @PostMapping
    public EntrepriseResponse createNewVersion(
            @Valid @RequestBody EntrepriseRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return entrepriseService.createNewVersion(user.id(), request);
    }

    @PostMapping("/update")
    public EntrepriseResponse updateCurrent(
            @RequestBody EntrepriseRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        return entrepriseService.updateCurrent(user.id(), request);
    }
}

