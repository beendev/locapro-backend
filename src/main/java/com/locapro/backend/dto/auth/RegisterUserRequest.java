package com.locapro.backend.dto.auth;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

public record RegisterUserRequest(

        @NotBlank(message = "Le prénom est obligatoire")
        String prenom,

        @NotBlank(message = "Le nom est obligatoire")
        String nom,

        @Email(message = "Format d'email invalide")
        @NotBlank(message = "L'email est obligatoire")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        @Pattern(
                // Nouvelle Regex "Tout Terrain" :
                // ^                 : Début
                // (?=.*[0-9])       : Au moins un chiffre
                // (?=.*[a-z])       : Au moins une minuscule
                // (?=.*[A-Z])       : Au moins une majuscule
                // (?=.*[^a-zA-Z0-9]): Au moins un caractère "spécial" (tout sauf lettre/chiffre)
                // .{8,}             : 8 caractères min
                // $                 : Fin
                regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{8,}$",
                message = "Le mot de passe doit contenir au moins 8 caractères, une majuscule, une minuscule, un chiffre et un caractère spécial."
        )
        String motDePasse,

        @Past(message = "La date de naissance doit être dans le passé")
        LocalDate dateNaissance,

        // Optionnel
        String numeroIpi,

        // Adresse
        String rue,
        String numero,
        String boite,
        String codePostal,
        String ville,
        String commune,
        String pays,

        Double latitude,
        Double longitude,
        String telephone
) {}