// com.locapro.backend.entity.PasswordResetTokenEntity
package com.locapro.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetTokenEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private UUID token;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private UtilisateurEntity utilisateur;

    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;

    @Column(nullable = false)
    private boolean used = false;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    // === getters manuels ===
    public Long getId() { return id; }
    public UUID getToken() { return token; }
    public UtilisateurEntity getUtilisateur() { return utilisateur; }
    public OffsetDateTime getExpiresAt() { return expiresAt; }
    public boolean isUsed() { return used; }
    public OffsetDateTime getCreatedAt() { return createdAt; }

    // === setters que tu avais déjà ===
    public void setToken(UUID token) { this.token = token; }
    public void setUtilisateur(UtilisateurEntity utilisateur) { this.utilisateur = utilisateur; }
    public void setExpiresAt(OffsetDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setUsed(boolean used) { this.used = used; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
