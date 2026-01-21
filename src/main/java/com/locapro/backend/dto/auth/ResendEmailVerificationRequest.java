package com.locapro.backend.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResendEmailVerificationRequest(

        @NotBlank(message = "L'adresse e-mail est obligatoire")
        @Email(message = "Le format de l'adresse e-mail est invalide")
        String email

) {}