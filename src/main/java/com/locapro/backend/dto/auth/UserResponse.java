package com.locapro.backend.dto.auth;

import java.time.LocalDate;

public class UserResponse {
    private Long id;
    private String prenom;
    private String nom;
    private String email;
    private LocalDate dateNaissance;
    private String numeroIpi;      // peut être null
    private boolean emailVerified; // false à l'inscription
    private boolean hasEntreprise; // false tant que non créée

    // Getters / Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }

    public String getNumeroIpi() { return numeroIpi; }
    public void setNumeroIpi(String numeroIpi) { this.numeroIpi = numeroIpi; }

    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }

    public boolean isHasEntreprise() { return hasEntreprise; }
    public void setHasEntreprise(boolean hasEntreprise) { this.hasEntreprise = hasEntreprise; }
}
