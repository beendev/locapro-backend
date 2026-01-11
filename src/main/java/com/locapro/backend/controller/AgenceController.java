package com.locapro.backend.controller;

import com.locapro.backend.dto.agence.AgenceInvitationResponse;
import com.locapro.backend.dto.agence.AgenceRequest;
import com.locapro.backend.dto.agence.AgenceResponse;
import com.locapro.backend.dto.agence.InviterGestionnaireRequest;
import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.security.JwtAuthFilter.UserPrincipal;
import com.locapro.backend.service.AgenceMembreService;
import com.locapro.backend.service.AgenceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agences")
public class AgenceController {

    private final AgenceService service;
    private final AgenceMembreService agenceMembreService;

    public AgenceController(AgenceService service, AgenceMembreService agenceMembreService) {
        this.service = service;
        this.agenceMembreService = agenceMembreService;
    }

    /** Crée une agence liée à l’entreprise de l’utilisateur connecté */
    @PostMapping
    public ResponseEntity<AgenceResponse> create(
            @Valid @RequestBody AgenceRequest req,
            Authentication auth
    ) {
        var p = (UserPrincipal) auth.getPrincipal();
        var created = service.create(p.id(), req);
        return ResponseEntity.ok(created);
    }

    /** Récupère l’agence courante de l’utilisateur connecté */
    @GetMapping("/current")
    public ResponseEntity<AgenceResponse> getCurrent(Authentication auth) {
        var p = (UserPrincipal) auth.getPrincipal();
        return ResponseEntity.ok(service.getCurrent(p.id()));
    }

    /** Invite un gestionnaire dans une agence donnée */
    @PostMapping("/{agenceId}/invitations")
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

    /**
     * Liste des invitations EN_ATTENTE pour une agence donnée.
     * GET /agences/{agenceId}/invitations/en-attente
     */
    @GetMapping("/{agenceId}/invitations/en-attente")
    public ResponseEntity<List<AgenceInvitationResponse>> listInvitationsEnAttente(
            @PathVariable Long agenceId,
            Authentication auth
    ) {
        var p = (UserPrincipal) auth.getPrincipal();
        var list = agenceMembreService.listerInvitationsEnAttente(agenceId, p.id());
        return ResponseEntity.ok(list);
    }

    /**
     * Annuler une invitation par son id.
     * POST /agences/invitations/{invitationId}/annuler
     */
    @PostMapping("/invitations/{invitationId}/annuler")
    public ResponseEntity<ApiMessageResponse> annulerInvitation(
            @PathVariable Long invitationId,
            Authentication auth
    ) {
        var p = (UserPrincipal) auth.getPrincipal();
        var resp = agenceMembreService.annulerInvitation(invitationId, p.id());
        return ResponseEntity.ok(resp);
    }

    /** L’utilisateur invité accepte une invitation */
    @PostMapping("/invitations/{invitationId}/accepter")
    public ResponseEntity<ApiMessageResponse> accepterInvitation(
            @PathVariable Long invitationId,
            Authentication auth
    ) {
        var p = (UserPrincipal) auth.getPrincipal();
        var resp = agenceMembreService.accepterInvitation(invitationId, p.id());
        return ResponseEntity.ok(resp);
    }

    /** L’utilisateur invité refuse une invitation */
    @PostMapping("/invitations/{invitationId}/refuser")
    public ResponseEntity<ApiMessageResponse> refuserInvitation(
            @PathVariable Long invitationId,
            Authentication auth
    ) {
        var p = (UserPrincipal) auth.getPrincipal();
        var resp = agenceMembreService.refuserInvitation(invitationId, p.id());
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/{agenceId}/quitter")
    public ResponseEntity<ApiMessageResponse> quitterAgence(
            @PathVariable Long agenceId,
            Authentication authentication
    ) {
        var p = (UserPrincipal) authentication.getPrincipal();

        var resp = agenceMembreService.quitterAgence(p.id(), agenceId);

        return ResponseEntity.ok(resp);
    }

}
