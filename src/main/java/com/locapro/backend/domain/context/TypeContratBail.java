package com.locapro.backend.domain.context;

public enum TypeContratBail {
    // Résidentiel
    CLASSIQUE_9ANS,      // Le standard
    COURTE_DUREE,        // 3 ans ou moins
    A_VIE,              // Rare, mais existe

    // Spécifique
    ETUDIANT,           // Kot (règles différentes par région)
    COLOCATION,         // Pacte de colocation

    // Professionnel
    COMMERCIAL,         // Commerce de détail
    BUREAU,             // Droit commun
    POP_UP_STORE        // Bail commercial courte durée
}