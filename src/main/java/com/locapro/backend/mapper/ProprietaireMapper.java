package com.locapro.backend.mapper;

import com.locapro.backend.domain.context.BienOwnershipMode;
import com.locapro.backend.dto.bien.BienOwnershipRequest;
import com.locapro.backend.entity.ProprietaireBienEntity;
import com.locapro.backend.entity.UtilisateurEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class ProprietaireMapper {

    public void applyOwnership(ProprietaireBienEntity lien, BienOwnershipRequest req, UtilisateurEntity currentUser) {
        if (lien == null || req == null || req.mode() == null) return;

        BienOwnershipMode mode = req.mode();

        // 1. GOMME MAGIQUE : On nettoie tout avant de remplir pour éviter les mélanges
        resetFields(lien);

        switch (mode) {
            case SELF -> {
                // Cas : Je suis le propriétaire (Récupération depuis mon profil User)
                if (currentUser == null) throw new IllegalArgumentException("Utilisateur courant requis pour mode SELF");

                lien.setProprietaireType("PERSONNE");
                lien.setProprietaireUtilisateurId(currentUser.getId());
                lien.setProprietaireNom(currentUser.getNom());
                lien.setProprietairePrenom(currentUser.getPrenom());
                lien.setProprietaireEmail(currentUser.getEmail());
                lien.setTelephone(currentUser.getTelephone());

                // On essaie de récupérer l'adresse du user s'il l'a renseignée dans son profil
                lien.setAdresseRue(currentUser.getRue());
                lien.setAdresseNumero(currentUser.getNumero());
                lien.setAdresseBoite(currentUser.getBoite());
                lien.setAdresseCodePostal(currentUser.getCodePostal());
                lien.setAdresseVille(currentUser.getVille());
                lien.setAdresseCommune(currentUser.getCommune());
                lien.setAdressePays(currentUser.getPays());

                // Date de naissance du User (si dispo)
                if (currentUser.getDateNaissance() != null) {
                    lien.setProprietaireDateNaissance(LocalDate.parse(currentUser.getDateNaissance().toString()));
                }
            }

            case PERSONNE_PHYSIQUE -> {
                // Cas : Agence encode un client particulier
                var p = req.personne();
                if (p != null) {
                    lien.setProprietaireType("PERSONNE");
                    lien.setProprietaireNom(p.nom());
                    lien.setProprietairePrenom(p.prenom());
                    lien.setProprietaireEmail(p.email());
                    lien.setTelephone(p.telephone());

                    // Gestion sécurisée de la date (évite le "null" en string)
                    if (p.dateNaissance() != null) {
                        lien.setProprietaireDateNaissance(LocalDate.parse(p.dateNaissance().toString()));
                    }
                    lien.setProprietaireLieuNaissance(p.lieuNaissance());

                    // Adresse
                    lien.setAdresseRue(p.rue());
                    lien.setAdresseNumero(p.numero());
                    lien.setAdresseBoite(p.boite());
                    lien.setAdresseCodePostal(p.codePostal());
                    lien.setAdresseVille(p.ville());
                    lien.setAdresseCommune(p.commune());
                    lien.setAdressePays(p.pays());
                }
            }

            case ENTREPRISE -> {
                // Cas : Société
                var e = req.entreprise();
                if (e != null) {
                    lien.setProprietaireType("ENTREPRISE");
                    lien.setProprietaireEntrepriseNom(e.raisonSociale());
                    lien.setProprietaireEmail(e.email());
                    lien.setNumeroBce(e.numeroTva());
                    lien.setRepresentantLegal(e.representantLegal());
                    lien.setTelephone(e.telephone());

                    // Siège Social
                    lien.setAdresseRue(e.rue());
                    lien.setAdresseNumero(e.numero());
                    lien.setAdresseBoite(e.boite());
                    lien.setAdresseCodePostal(e.codePostal());
                    lien.setAdresseVille(e.ville());
                    lien.setAdresseCommune(e.commune());
                    lien.setAdressePays(e.pays());
                }
            }
        }
    }

    private void resetFields(ProprietaireBienEntity lien) {
        // Reset Identité Personne
        lien.setProprietaireUtilisateurId(null);
        lien.setProprietaireNom(null);
        lien.setProprietairePrenom(null);
        lien.setProprietaireDateNaissance(null); // NOUVEAU
        lien.setProprietaireLieuNaissance(null); // NOUVEAU

        // Reset Identité Entreprise
        lien.setProprietaireEntrepriseNom(null);
        lien.setNumeroBce(null);
        lien.setRepresentantLegal(null);

        // Reset Contact & Adresse (Commun)
        lien.setProprietaireEmail(null);
        lien.setTelephone(null);
        lien.setAdresseRue(null);
        lien.setAdresseNumero(null);
        lien.setAdresseBoite(null);
        lien.setAdresseCodePostal(null);
        lien.setAdresseVille(null);
        lien.setAdresseCommune(null);
        lien.setAdressePays(null);
    }
}