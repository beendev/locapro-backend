package com.locapro.backend.mapper;


import com.locapro.backend.dto.bien.BienInfosDeBaseRequest;
import com.locapro.backend.dto.bien.BienParentInfosRequest;
import com.locapro.backend.entity.BienEntity;
import org.springframework.stereotype.Component;

@Component
public class BienEntityMapper {

    // --- CRÉATION PARENT ---
    public BienEntity toParentEntity(BienParentInfosRequest req, Long agenceId) {
        if (req == null) return null;

        BienEntity parent = new BienEntity();
        parent.setNomReference(req.nomReferenceInterne());
        parent.setLibelleUnite(req.libelleVisible());
        parent.setTypeBien(req.typeBien().name());
        parent.setSousType(req.sousType() != null ? req.sousType().name() : null);

        // Champs techniques par défaut pour un parent
        parent.setStatut("ACTIF");
        parent.setEstUniteLocative(false);
        parent.setParentBienId(null);
        parent.setEnabled(true);
        parent.setAgenceId(agenceId);

        // Mapping Adresse
        mapAdresse(parent, req.rue(), req.numero(), req.boite(), req.codePostal(), req.ville(), req.pays(), req.latitude(), req.longitude());

        return parent;
    }

    // --- CRÉATION ENFANT / UNITÉ ---
    public BienEntity toUniteEntity(BienInfosDeBaseRequest req, BienEntity parent, Long agenceId, boolean estLocatif) {
        if (req == null) return null;

        BienEntity unite = new BienEntity();
        unite.setNomReference(req.nomReferenceInterne());
        unite.setLibelleUnite(req.libelleVisible());
        unite.setTypeBien(req.typeBien().name());
        unite.setSousType(req.sousType() != null ? req.sousType().name() : null);

        unite.setNumeroPorte(req.numeroPorte());
        unite.setBoiteUnite(req.boiteUnite());

        unite.setEstUniteLocative(estLocatif);
        unite.setParentBienId(parent != null ? parent.getId() : null);
        unite.setEnabled(true);
        unite.setStatut("ACTIF");
        unite.setAgenceId(agenceId);

        // Pour l'enfant, l'adresse est souvent celle du parent, mais on mappe quand même si fourni
        // (Note: dans ta logique actuelle, l'enfant hérite souvent de l'adresse du parent,
        // tu peux choisir de ne pas mapper l'adresse ici si tu veux garder la logique stricte)

        return unite;
    }

    // --- UPDATE PARENT ---
    public void updateParent(BienEntity parent, BienParentInfosRequest req) {
        if (parent == null || req == null) return;

        if (req.libelleVisible() != null) parent.setLibelleUnite(req.libelleVisible());
        // On ne change pas le type structurel d'un parent

        mapAdresse(parent, req.rue(), req.numero(), req.boite(), req.codePostal(), req.ville(), req.pays(), req.latitude(), req.longitude());
    }

    // --- UPDATE UNITÉ ---
    // --- UPDATE UNITÉ (Version Corrigée) ---
    public void updateUnite(BienEntity unite, BienInfosDeBaseRequest req) {
        if (unite == null || req == null) return;

        // 1. Infos Générales
        if (req.libelleVisible() != null) unite.setLibelleUnite(req.libelleVisible());
        if (req.numeroPorte() != null) unite.setNumeroPorte(req.numeroPorte());
        if (req.boiteUnite() != null) unite.setBoiteUnite(req.boiteUnite());
        if (req.estUniteLocative() != null) unite.setEstUniteLocative(req.estUniteLocative());

        // 2. Adresse (Peut être différente du parent pour une maison ou un commerce)
        // On utilise des 'if' pour permettre des mises à jour partielles
        if (req.rue() != null) unite.setRue(req.rue());
        if (req.numero() != null) unite.setNumero(req.numero());
        if (req.boite() != null) unite.setBoite(req.boite());
        if (req.codePostal() != null) unite.setCodePostal(req.codePostal());
        if (req.ville() != null) unite.setVille(req.ville());
        if (req.pays() != null) unite.setPays(req.pays());
    }
    // --- HELPER ADRESSE (Privé) ---
    private void mapAdresse(BienEntity b, String rue, String num, String bte, String cp, String ville, String pays, Double lat, Double lon) {
        if (rue != null) b.setRue(rue);
        if (num != null) b.setNumero(num);
        if (bte != null) b.setBoite(bte);
        if (cp != null) b.setCodePostal(cp);
        if (ville != null) b.setVille(ville);
        if (pays != null) b.setPays(pays);
        // Lat/Long souvent null, on ne les écrase pas si null
        if (lat != null) b.setLatitude(lat);
        if (lon != null) b.setLongitude(lon);
    }

}