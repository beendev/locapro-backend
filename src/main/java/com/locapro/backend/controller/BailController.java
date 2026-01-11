package com.locapro.backend.controller;

import com.locapro.backend.dto.bail.BailResponse;
import com.locapro.backend.dto.bail.CreateBailRequest;
import com.locapro.backend.service.BailService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import com.locapro.backend.security.JwtAuthFilter.UserPrincipal;
import java.security.Principal;

@RestController
@RequestMapping("/baux")
public class BailController {

    private final BailService bailService;

    public BailController(BailService bailService) {
        this.bailService = bailService;
    }

    @PostMapping
    public ResponseEntity<BailResponse> creerBail(
            @RequestBody @Valid CreateBailRequest request,
            @AuthenticationPrincipal UserPrincipal currentUser
    ) {
        // TODO: adapte cette extraction à TONS système
        // Exemple si principal.getName() = userId en string :

        BailResponse response = bailService.creerBail(currentUser.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
