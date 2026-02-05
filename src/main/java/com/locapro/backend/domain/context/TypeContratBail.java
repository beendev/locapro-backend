package com.locapro.backend.domain.context;

public enum TypeContratBail {
    // 🏠 LOGEMENT PRINCIPAL (Loi sur les baux de résidence principale)
    RESIDENCE_PRINCIPALE,

    // 🎓 ETUDIANT (Kot - Décret spécifique par région)
    ETUDIANT,

    // 🤝 COLOCATION (Pacte de colocation obligatoire à BXL/WAL)
    COLOCATION,

    // 🏢 DROIT COMMUN (Résidence secondaire, pied-à-terre, bureau non-comm.)
    DROIT_COMMUN,

    // 🛍️ COMMERCIAL (Loi sur les baux commerciaux)
    COMMERCIAL,

    // 📦 POP-UP (Bail commercial de courte durée)
    POP_UP_STORE,

    // 🚗 GARAGE / PARKING (Si loué seul)
    GARAGE
}