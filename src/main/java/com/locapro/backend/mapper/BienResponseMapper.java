package com.locapro.backend.mapper;

import com.locapro.backend.dto.bien.*;
import com.locapro.backend.entity.*;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class BienResponseMapper {

    // ====================================================================
    // 1. MAPPING COMPLET (Détails + Propriétaires + Parent + Colocation)
    // ====================================================================
    public BienResponse toBienComplet(
            BienEntity unite,
            BienEntity parent,
            DetailsResidentielEntity residentiel,
            DetailsCommerceEntity commerce,
            DetailsBureauEntity bureau,
            DetailsParkingEntity parking,
            DetailsColocationEntity colocation,
            ProprietaireBienEntity proprietaire,
            ProprietaireBienEntity proprietaireParent
    ) {
        if (unite == null) return null;

        // --- Logique Adresse ---
        // Priorité au parent pour l'adresse principale (immeuble), sinon l'unité (maison)
        String rue = parent != null ? parent.getRue() : unite.getRue();
        String numero = parent != null ? parent.getNumero() : unite.getNumero();
        String boiteAdresse = parent != null ? parent.getBoite() : unite.getBoite();
        String cp = parent != null ? parent.getCodePostal() : unite.getCodePostal();
        String ville = parent != null ? parent.getVille() : unite.getVille();
        String commune = parent != null ? parent.getCommune() : unite.getCommune();
        String pays = parent != null ? parent.getPays() : unite.getPays();

        Double lat = parent != null ? parent.getLatitude() : unite.getLatitude();
        Double lon = parent != null ? parent.getLongitude() : unite.getLongitude();

        // --- Construction ---
        return buildBienResponse(
                unite,
                rue, numero, boiteAdresse, cp, ville, commune, pays, lat, lon,
                parent,
                mapResidentiel(residentiel),
                mapCommerce(commerce),
                mapBureau(bureau),
                mapParking(parking),
                mapColocation(colocation),
                mapProprietaire(proprietaire),
                mapProprietaire(proprietaireParent)
        );
    }

    // ====================================================================
    // 2. MAPPING LÉGER / SUMMARY (Liste rapide)
    // ====================================================================
    public BienResponse toBienSummary(BienEntity unite) {
        if (unite == null) return null;

        // Pour le sommaire, on prend l'adresse brute de l'unité (plus rapide)
        return buildBienResponse(
                unite,
                unite.getRue(), unite.getNumero(), unite.getBoite(),
                unite.getCodePostal(), unite.getVille(), unite.getCommune(), unite.getPays(),
                unite.getLatitude(), unite.getLongitude(),
                null, // Pas de parent chargé
                null, null, null, null, null, // Pas de détails (res, com, bur, park, coloc)
                null, null // Pas de propriétaires
        );
    }

    // ====================================================================
    // 3. MÉTHODE CENTRALE DE CONSTRUCTION (DRY - Don't Repeat Yourself)
    // ====================================================================
    private BienResponse buildBienResponse(
            BienEntity u,
            String rue, String num, String bte, String cp, String ville, String commune, String pays, Double lat, Double lon,
            BienEntity parent,
            DetailsResidentielResponse res,
            DetailsCommerceResponse com,
            DetailsBureauResponse bur,
            DetailsParkingResponse park,
            DetailsColocationResponse coloc,
            ProprietaireBienResponse prop,
            ProprietaireBienResponse propParent
    ) {
        return new BienResponse(
                u.getId(),
                u.getNomReference(),
                u.getTypeBien(),
                u.getSousType(),
                u.getLibelleUnite(),
                u.getCodePublic() != null ? u.getCodePublic().toString() : null,
                u.isEstUniteLocative(),
                u.getPortefeuilleId(),
                rue, num, bte, u.getBoiteUnite(), cp, ville, commune, pays,
                lat, lon,
                u.getStatut(),
                u.getNotesIdentification(),
                parent != null ? parent.getId() : (u.getParentBienId()), // Si parent non chargé, on renvoie au moins l'ID
                parent != null ? parent.getNomReference() : null,
                parent != null ? parent.getLibelleUnite() : null,
                res, com, bur, park,
                prop, propParent,
                u.getDescription(),
                u.getRevenuCadastral(),
                coloc
        );
    }

    // ====================================================================
    // 4. HELPERS DE MAPPING DÉTAILS
    // ====================================================================

    private DetailsResidentielResponse mapResidentiel(DetailsResidentielEntity e) {
        if (e == null) return null;
        return new DetailsResidentielResponse(
                e.getSuperficieHabitableM2(), e.getNombreFacades(), e.getEtage(),
                e.getAnneeConstruction(), e.getAnneeRenovation(), e.getNbChambres(),
                e.getNbSallesBain(), e.getNbSallesDouche(), e.getNbWc(),
                e.getHallEntree(), e.getTypeCuisine(), e.getPebClasse(),
                e.getPebConsoKwhM2An(), e.getTypeChassis(), e.getTypeChauffage(),
                e.getElectriciteConforme(), e.getDetecteursFumee(), e.getMeuble(),
                e.getParlophone(), e.getAlarme(), e.getQualiteSols(),
                e.getJardin(), e.getJardinSurfaceM2(),
                e.getTerrasse(), e.getTerrasseSurfaceM2(),
                e.getBalcon(), e.getCave(), e.getGrenier(),
                e.getPebNumero(), e.getPebDateValidite()
        );
    }

    private DetailsCommerceResponse mapCommerce(DetailsCommerceEntity e) {
        if (e == null) return null;
        return new DetailsCommerceResponse(
                e.getSurfaceCommercialeM2(), e.getSurfaceVitrineM2(),
                e.getSurfaceReserveM2(), e.getExtractionHoreca()
        );
    }

    private DetailsBureauResponse mapBureau(DetailsBureauEntity e) {
        if (e == null) return null;
        return new DetailsBureauResponse(
                e.getSurfaceBureauxM2(), e.getNbBureauxCloisonnes(),
                e.getSalleReunion(), e.getCablageInformatique()
        );
    }

    private DetailsParkingResponse mapParking(DetailsParkingEntity e) {
        if (e == null) return null;
        return new DetailsParkingResponse(
                e.getNumeroPlace(), e.getLongueurM(),
                e.getLargeurM(), e.getTypePorte(), e.getPriseElectrique()
        );
    }

    private DetailsColocationResponse mapColocation(DetailsColocationEntity e) {
        if (e == null) return null;
        return new DetailsColocationResponse(
                e.getCuisineCommune(), e.getSalonCommun(),
                e.getSdbCommune(), e.getWcCommun(),
                e.getBuanderieCommune(), e.getJardinCommun(),
                e.getTerrasseCommune(), e.getDescriptionCommunes()
        );
    }

    private ProprietaireBienResponse mapProprietaire(ProprietaireBienEntity e) {
        if (e == null) return null;
        LocalDate dateNaiss = e.getProprietaireDateNaissance();
        return new ProprietaireBienResponse(
                e.getProprietaireType(), e.getProprietaireNom(), e.getProprietairePrenom(),
                e.getProprietaireEmail(), dateNaiss, e.getProprietaireLieuNaissance(),
                e.getProprietaireEntrepriseNom(), e.getNumeroBce(), e.getRepresentantLegal(),
                e.getTelephone(), e.getAdresseRue(), e.getAdresseNumero(),
                e.getAdresseBoite(), e.getAdresseCodePostal(), e.getAdresseVille(),
                e.getAdresseCommune(), e.getAdressePays()
        );
    }
}