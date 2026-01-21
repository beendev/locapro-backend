package com.locapro.backend.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(

        @NotBlank(message = "L'ancien mot de passe est obligatoire")
        String oldPassword,

        @NotBlank(message = "Le nouveau mot de passe est obligatoire")
        @Pattern(

                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[.@#$%^&+=!_\\-*]).{8,}$",
                message = "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial (ex: . @ # $ %)."
        )
        String newPassword
) {}