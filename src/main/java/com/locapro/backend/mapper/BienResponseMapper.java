package com.locapro.backend.mapper;

import com.locapro.backend.dto.bien.*;
import com.locapro.backend.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class BienResponseMapper {



    public BienResponse toBienComplet(
            BienEntity unite,
            BienEntity parent,
            DetailsResidentielEntity residentielEntity,
            DetailsCommerceEntity commerceEntity,
            DetailsBureauEntity bureauEntity,
            DetailsParkingEntity parkingEntity,
            ProprietaireBienEntity proprietaireEntity,       // Proprio Enfant
            ProprietaireBienEntity proprietaireParentEntity  // Proprio Parent
    ) {
        if (unite == null) return null;

        // 1. Adresse : Priorité au parent
        String rue = parent != null ? parent.getRue() : unite.getRue();
        String numero = parent != null ? parent.getNumero() : unite.getNumero();
        String boiteAdresse = parent != null ? parent.getBoite() : unite.getBoite();
        String codePostal = parent != null ? parent.getCodePostal() : unite.getCodePostal();
        String ville = parent != null ? parent.getVille() : unite.getVille();
        String pays = parent != null ? parent.getPays() : unite.getPays();

        // Champs spécifiques à l'unité
        String boiteUnite = unite.getBoiteUnite();

        // 👇 Mapping GPS (Priorité Parent car l'immeuble définit la position)
        Double latitude = parent != null ? parent.getLatitude() : unite.getLatitude();
        Double longitude = parent != null ? parent.getLongitude() : unite.getLongitude();

        // 2. Mapping des Détails
        DetailsResidentielResponse resDTO = null;
        if (residentielEntity != null) {
            resDTO = new DetailsResidentielResponse(
                    residentielEntity.getSuperficieHabitableM2(),
                    residentielEntity.getNombreFacades(),
                    residentielEntity.getEtage(),
                    residentielEntity.getAnneeConstruction(),
                    residentielEntity.getAnneeRenovation(),
                    residentielEntity.getNbChambres(),
                    residentielEntity.getNbSallesBain(),
                    residentielEntity.getNbSallesDouche(),
                    residentielEntity.getNbWc(),
                    residentielEntity.getHallEntree(),
                    residentielEntity.getTypeCuisine(),
                    residentielEntity.getPebClasse(),
                    residentielEntity.getPebConsoKwhM2An(),
                    residentielEntity.getTypeChassis(),
                    residentielEntity.getTypeChauffage(),
                    residentielEntity.getElectriciteConforme(),
                    residentielEntity.getDetecteursFumee(),
                    residentielEntity.getMeuble(),
                    residentielEntity.getParlophone(),
                    residentielEntity.getAlarme(),
                    residentielEntity.getQualiteSols(),
                    residentielEntity.getJardin(),
                    residentielEntity.getJardinSurfaceM2(),
                    residentielEntity.getTerrasse(),
                    residentielEntity.getTerrasseSurfaceM2(),
                    residentielEntity.getBalcon(),
                    residentielEntity.getCave(),
                    residentielEntity.getGrenier()
            );
        }

        DetailsCommerceResponse comDTO = null;
        if (commerceEntity != null) {
            comDTO = new DetailsCommerceResponse(
                    commerceEntity.getSurfaceCommercialeM2(),
                    commerceEntity.getSurfaceVitrineM2(),
                    commerceEntity.getSurfaceReserveM2(),
                    commerceEntity.getExtractionHoreca()
            );
        }

        DetailsBureauResponse burDTO = null;
        if (bureauEntity != null) {
            burDTO = new DetailsBureauResponse(
                    bureauEntity.getSurfaceBureauxM2(),
                    bureauEntity.getNbBureauxCloisonnes(),
                    bureauEntity.getSalleReunion(),
                    bureauEntity.getCablageInformatique()
            );
        }

        DetailsParkingResponse parkDTO = null;
        if (parkingEntity != null) {
            parkDTO = new DetailsParkingResponse(
                    parkingEntity.getNumeroPlace(),
                    parkingEntity.getLongueurM(),
                    parkingEntity.getLargeurM(),
                    parkingEntity.getTypePorte(),
                    parkingEntity.getPriseElectrique()
            );
        }

        // 3. Mapping des Propriétaires
        ProprietaireBienResponse propDTO = mapProprietaire(proprietaireEntity);
        ProprietaireBienResponse propParentDTO = mapProprietaire(proprietaireParentEntity);

        // 4. Construction finale
        return new BienResponse(
                unite.getId(),
                unite.getNomReference(),
                unite.getTypeBien(),
                unite.getSousType(),
                unite.getLibelleUnite(),
                unite.getCodePublic() != null ? unite.getCodePublic().toString() : null,
                unite.isEstUniteLocative(),
                unite.getPortefeuilleId(),

                // Adresse
                rue,
                numero,
                boiteAdresse,
                boiteUnite,
                codePostal,
                ville,
                pays,

                // 👇 Nouveaux champs mappés
                latitude,
                longitude,
                unite.getStatut(),
                unite.getNotesIdentification(),

                // Parent
                parent != null ? parent.getId() : null,
                parent != null ? parent.getNomReference() : null,
                parent != null ? parent.getLibelleUnite() : null,

                // Détails
                resDTO,
                comDTO,
                burDTO,
                parkDTO,

                // Propriétaires
                propDTO,
                propParentDTO
        );
    }

    // --- Helper Privé ---
    private ProprietaireBienResponse mapProprietaire(ProprietaireBienEntity entity) {
        if (entity == null) return null;

        LocalDate dateNaiss = entity.getProprietaireDateNaissance();

        return new ProprietaireBienResponse(
                entity.getProprietaireType(),
                entity.getProprietaireNom(),
                entity.getProprietairePrenom(),
                entity.getProprietaireEmail(),
                dateNaiss,
                entity.getProprietaireLieuNaissance(),
                entity.getProprietaireEntrepriseNom(),
                entity.getNumeroBce(),
                entity.getRepresentantLegal(),
                entity.getTelephone(),
                entity.getAdresseRue(),
                entity.getAdresseNumero(),
                entity.getAdresseBoite(),
                entity.getAdresseCodePostal(),
                entity.getAdresseVille(),
                entity.getAdressePays()
        );
    }

    public BienResponse toBienSummary(BienEntity unite) {
        if (unite == null) return null;

        return new BienResponse(
                unite.getId(),
                unite.getNomReference(),
                unite.getTypeBien(),
                unite.getSousType(),
                unite.getLibelleUnite(),
                unite.getCodePublic() != null ? unite.getCodePublic().toString() : null,
                unite.isEstUniteLocative(),
                unite.getPortefeuilleId(),

                // Adresse (On prend celle stockée dans l'unité)
                unite.getRue(),
                unite.getNumero(),
                unite.getBoite(),       // boiteAdresse
                unite.getBoiteUnite(),  // boiteUnite
                unite.getCodePostal(),
                unite.getVille(),
                unite.getPays(),

                // Infos GPS & Statut
                unite.getLatitude(),
                unite.getLongitude(),
                unite.getStatut(),
                unite.getNotesIdentification(),

                // Parent (On renvoie juste l'ID, pas les noms pour éviter une requête SQL en plus)
                unite.getParentBienId(),
                null, // parentNomReference (non chargé)
                null, // parentLibelle (non chargé)

                // Détails Techniques (On laisse NULL pour alléger)
                null, // Residentiel
                null, // Commerce
                null, // Bureau
                null, // Parking

                // Propriétaires (On laisse NULL)
                null, // Proprio
                null  // Proprio Parent
        );
    }
}