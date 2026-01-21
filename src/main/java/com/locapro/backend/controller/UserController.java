package com.locapro.backend.controller;

import com.locapro.backend.dto.user.UserResponse;
import com.locapro.backend.dto.common.ApiMessageResponse;
import com.locapro.backend.dto.user.ChangePasswordRequest;
import com.locapro.backend.dto.user.UpdateUserProfileRequest;
import com.locapro.backend.security.JwtAuthFilter.UserPrincipal;
import com.locapro.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@Tag(name = "Gestion Utilisateur", description = "Endpoints pour la gestion du profil et du compte")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // --- 1. Voir son profil ---
    @GetMapping("/me")
    @Operation(summary = "Mon Profil", description = "Récupère les détails de l'utilisateur connecté.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> getCurrentUser( @AuthenticationPrincipal UserPrincipal currentUser) {

        return ResponseEntity.ok(userService.getCurrentUser(currentUser.id()));
    }

    // --- 2. Mettre à jour son profil ---
    @PostMapping("/me")
    @Operation(summary = "Mise à jour Profil", description = "Met à jour les informations personnelles (sauf Email/IPI).", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody @Valid UpdateUserProfileRequest request
    ) {

        return ResponseEntity.ok(userService.updateProfile(currentUser.id(), request));
    }

    // --- 3. Changer mot de passe ---
    @PostMapping("/me/password")
    @Operation(summary = "Changer mot de passe", description = "Permet de modifier son mot de passe.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiMessageResponse> changePassword(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestBody @Valid ChangePasswordRequest request
    ) {

        var resp = userService.changePassword(currentUser.id(), request);
        return ResponseEntity.ok(resp);
    }

    // --- 4. Droit à l'oubli ---
    @DeleteMapping("/me")
    @Operation(summary = "Supprimer mon compte", description = "Anonymisation irréversible (RGPD).", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiMessageResponse> deleteAccount(@AuthenticationPrincipal UserPrincipal currentUser) {

        var resp = userService.anonymiserUser(currentUser.id());
        return ResponseEntity.ok(resp);
    }
}