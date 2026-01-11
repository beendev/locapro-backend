package com.locapro.backend.domain.context;

public enum BienOwnershipMode {
    SELF,               // le bien appartient à l'utilisateur courant (contexte PROPRIETAIRE)
    PERSONNE_PHYSIQUE,  // propriétaire = une personne externe
    ENTREPRISE          // propriétaire = une entreprise externe
}
