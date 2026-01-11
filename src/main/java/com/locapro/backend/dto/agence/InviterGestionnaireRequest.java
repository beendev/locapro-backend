package com.locapro.backend.dto.agence;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record InviterGestionnaireRequest(
        @NotBlank @Email String email
) {}
