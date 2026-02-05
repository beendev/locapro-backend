package com.locapro.backend.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "compteurs_bien")
public class CompteurBienEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bien_id", nullable = false)
    private Long bienId;

    // ELECTRICITE, GAZ, EAU
    @Column(name = "type_compteur", nullable = false)
    private String typeCompteur;

    @Column(name = "numero_compteur")
    private String numeroCompteur;

    // Le nouveau champ qu'on vient d'ajouter en SQL !
    @Column(name = "code_ean")
    private String codeEan;

    // --- GETTERS & SETTERS ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBienId() { return bienId; }
    public void setBienId(Long bienId) { this.bienId = bienId; }

    public String getTypeCompteur() { return typeCompteur; }
    public void setTypeCompteur(String typeCompteur) { this.typeCompteur = typeCompteur; }

    public String getNumeroCompteur() { return numeroCompteur; }
    public void setNumeroCompteur(String numeroCompteur) { this.numeroCompteur = numeroCompteur; }

    public String getCodeEan() { return codeEan; }
    public void setCodeEan(String codeEan) { this.codeEan = codeEan; }
}