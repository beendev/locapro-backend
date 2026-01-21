package com.locapro.backend.controller;

import com.locapro.backend.dto.agence.*;
import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.security.JwtAuthFilter.UserPrincipal;
import com.locapro.backend.service.AgenceMembreService;
import com.locapro.backend.service.AgenceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agences")
@Tag(name = "Gestion des Agences", description = "Endpoints pour la gestion des agences immobilières et des invitations de membres")
public class AgenceController {

    private final AgenceService service;
    private final AgenceMembreService agenceMembreService;

    public AgenceController(AgenceService service, AgenceMembreService agenceMembreService) {
        this.service = service;
        this.agenceMembreService = agenceMembreService;
    }

    @PostMapping
    @Operation(summary = "Créer une agence",
            description = "Crée une agence liée à l’entreprise de l’utilisateur connecté. L'utilisateur devient ADMIN_AGENCE.")
    public ResponseEntity<AgenceResponse> create(
            @Valid @RequestBody AgenceRequest req,
            Authentication auth
    ) {
        var p = (UserPrincipal) auth.getPrincipal();
        var created = service.create(p.id(), req);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/current")
    @Operation(summary = "Récupérer l'agence courante",
            description = "Retourne les informations de l'agence à laquelle l'utilisateur connecté est rattaché.")
    public ResponseEntity<AgenceResponse> getCurrent(Authentication auth) {
        var p = (UserPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(service.getCurrent(p.id()));
    }

    @PostMapping("/{agenceId}/invitations")
    @Operation(summary = "Inviter un gestionnaire",
            description = "Envoie une invitation par email pour rejoindre l'agence en tant que gestionnaire.")
    public ResponseEntity<ApiMessageResponse> inviterGestionnaire(
            @PathVariable Long agenceId,
            @Valid @RequestBody InviterGestionnaireRequest req,
            Authentication auth
    ) {
        var p = (UserPrincipal) auth.getPrincipal();
        ApiMessageResponse response = agenceMembreService
                .inviterGestionnaire(agenceId, req.email(), p.id());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{agenceId}/invitations/en-attente")
    @Operation(summary = "Lister les invitations en attente",
            description = "Récupère la liste des invitations qui n'ont pas encore été acceptées ou refusées pour une agence.")
    public ResponseEntity<List<AgenceInvitationResponse>> listInvitationsEnAttente(
            @PathVariable Long agenceId,
            Authentication auth
    ) {
        var p = (UserPrincipal) auth.getPrincipal();
        var list = agenceMembreService.listerInvitationsEnAttente(agenceId, p.id());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/invitations/{invitationId}/annuler")
    @Operation(summary = "Annuler une invitation",
            description = "Permet à l'administrateur de l'agence d'annuler une invitation envoyée.")
    public ResponseEntity<ApiMessageResponse> annulerInvitation(
            @PathVariable Long invitationId,
            Authentication auth
    ) {
        var p = (UserPrincipal) auth.getPrincipal();
        var resp = agenceMembreService.annulerInvitation(invitationId, p.id());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/invitations/{invitationId}/accepter")
    @Operation(summary = "Accepter une invitation",
            description = "L'utilisateur invité accepte de rejoindre l'agence.")
    public ResponseEntity<ApiMessageResponse> accepterInvitation(
            @PathVariable Long invitationId,
            Authentication auth
    ) {
        var p = (UserPrincipal) auth.getPrincipal();
        var resp = agenceMembreService.accepterInvitation(invitationId, p.id());
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/invitations/{invitationId}/refuser")
    @Operation(summary = "Refuser une invitation",
            description = "L'utilisateur invité décline l'invitation de l'agence.")
    public ResponseEntity<ApiMessageResponse> refuserInvitation(
            @PathVariable Long invitationId,
            Authentication auth
    ) {
        var p = (UserPrincipal) auth.getPrincipal();
        var resp = agenceMembreService.refuserInvitation(invitationId, p.id());
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{agenceId}/quitter")
    @Operation(summary = "Quitter l'agence",
            description = "Permet à un membre de quitter volontairement son agence actuelle.")
    public ResponseEntity<ApiMessageResponse> quitterAgence(
            @PathVariable Long agenceId,
            Authentication authentication
    ) {
        var p = (UserPrincipal) authentication.getPrincipal();
        var resp = agenceMembreService.quitterAgence(p.id(), agenceId);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/invitations/validate-token")
    @Operation(summary = "Accepter une invitation via Token",
            description = "Permet à l'utilisateur connecté d'accepter une invitation en fournissant le token reçu par email.")
    public ResponseEntity<ApiMessageResponse> acceptInvitationByToken(
            @RequestBody @Valid ValidateTokenRequest req,
            Authentication auth
    ) {
        var p = (UserPrincipal) auth.getPrincipal();

        // Plus besoin de body.get("token"), on a un objet typé
        agenceMembreService.accepterInvitationParToken(req.token(), p.id());

        return ResponseEntity.ok(new ApiMessageResponse("Invitation acceptée avec succès. Bienvenue dans l'équipe !"));
    }
}