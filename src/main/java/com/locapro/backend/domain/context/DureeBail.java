package com.locapro.backend.domain.context;

public enum DureeBail {
    // Pour Résidence Principale
    COURT_TERME,       // <= 3 ans
    STANDARD_9_ANS,    // 9 ans
    LONGUE_DUREE,      // > 9 ans
    A_VIE,             // Bail à vie

    // Pour Etudiant
    ANNEE_ACADEMIQUE,  // 10 ou 12 mois
    SECONDE_SESSION,   // 1 ou 2 mois

    // Pour Droit Commun / Commercial
    DUREE_DETERMINEE,
    INDETERMINEE
}