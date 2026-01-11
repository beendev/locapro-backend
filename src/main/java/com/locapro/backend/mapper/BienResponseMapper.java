package com.locapro.backend.mapper;

import com.locapro.backend.dto.bien.BienResponse;
import com.locapro.backend.dto.bien.DetailsBureauResponse;
import com.locapro.backend.dto.bien.DetailsCommerceResponse;
import com.locapro.backend.dto.bien.DetailsParkingResponse;
import com.locapro.backend.dto.bien.DetailsResidentielResponse;
import com.locapro.backend.dto.bien.ProprietaireBienResponse;
import com.locapro.backend.entity.BienEntity;
import com.locapro.backend.entity.DetailsBureauEntity;
import com.locapro.backend.entity.DetailsCommerceEntity;
import com.locapro.backend.entity.DetailsParkingEntity;
import com.locapro.backend.entity.DetailsResidentielEntity;
import com.locapro.backend.repository.DetailsBureauRepository;
import com.locapro.backend.repository.DetailsCommerceRepository;
import com.locapro.backend.repository.DetailsParkingRepository;
import com.locapro.backend.repository.DetailsResidentielRepository;
import com.locapro.backend.repository.ProprietaireBienRepository;
import org.springframework.stereotype.Component;

@Component
public class BienResponseMapper {

    private final DetailsResidentielRepository detailsResidentielRepository;
    private final DetailsCommerceRepository detailsCommerceRepository;
    private final DetailsBureauRepository detailsBureauRepository;
    private final DetailsParkingRepository detailsParkingRepository;
    private final ProprietaireBienRepository proprietaireBienRepository;

    public BienResponseMapper(DetailsResidentielRepository detailsResidentielRepository,
                              DetailsCommerceRepository detailsCommerceRepository,
                              DetailsBureauRepository detailsBureauRepository,
                              DetailsParkingRepository detailsParkingRepository,
                              ProprietaireBienRepository proprietaireBienRepository) {
        this.detailsResidentielRepository = detailsResidentielRepository;
        this.detailsCommerceRepository = detailsCommerceRepository;
        this.detailsBureauRepository = detailsBureauRepository;
        this.detailsParkingRepository = detailsParkingRepository;
        this.proprietaireBienRepository = proprietaireBienRepository;
    }

    public BienResponse toBienComplet(BienEntity unite, BienEntity parent) {

        // Adresse : priorité au parent (immeuble / maison)
        String rue = parent != null ? parent.getRue() : unite.getRue();
        String numero = parent != null ? parent.getNumero() : unite.getNumero();
        String boiteAdresse = parent != null ? parent.getBoite() : unite.getBoite();
        String codePostal = parent != null ? parent.getCodePostal() : unite.getCodePostal();
        String ville = parent != null ? parent.getVille() : unite.getVille();
        String pays = parent != null ? parent.getPays() : unite.getPays();

        // Boîte de l’unité (peut être null, c’est normal)
        String boiteUnite = unite.getBoiteUnite();

        // Détails selon le type
        DetailsResidentielResponse residentiel = null;
        DetailsCommerceResponse commerce = null;
        DetailsBureauResponse bureau = null;
        DetailsParkingResponse parking = null;

        switch (unite.getTypeBien()) {
            case "RESIDENTIEL" -> {
                DetailsResidentielEntity d = detailsResidentielRepository
                        .findById(unite.getId())
                        .orElse(null);
                if (d != null) {
                    residentiel = new DetailsResidentielResponse(d);
                }

                // Parking intégré éventuel
                DetailsParkingEntity p = detailsParkingRepository
                        .findById(unite.getId())
                        .orElse(null);
                if (p != null) {
                    parking = new DetailsParkingResponse(
                            p.getNumeroPlace(),
                            p.getLongueurM(),
                            p.getLargeurM(),
                            p.getTypePorte(),
                            p.getPriseElectrique()
                    );
                }
            }

            case "COMMERCE" -> {
                DetailsCommerceEntity d = detailsCommerceRepository
                        .findById(unite.getId())
                        .orElse(null);
                if (d != null) {
                    commerce = new DetailsCommerceResponse(
                            d.getSurfaceCommercialeM2(),
                            d.getSurfaceVitrineM2(),
                            d.getSurfaceReserveM2(),
                            d.getExtractionHoreca()
                    );
                }
            }

            case "BUREAU" -> {
                DetailsBureauEntity d = detailsBureauRepository
                        .findById(unite.getId())
                        .orElse(null);
                if (d != null) {
                    bureau = new DetailsBureauResponse(
                            d.getSurfaceBureauxM2(),
                            d.getNbBureauxCloisonnes(),
                            d.getSalleReunion(),
                            d.getCablageInformatique()
                    );
                }
            }

            case "PARKING" -> {
                DetailsParkingEntity p = detailsParkingRepository
                        .findById(unite.getId())
                        .orElse(null);
                if (p != null) {
                    parking = new DetailsParkingResponse(
                            p.getNumeroPlace(),
                            p.getLongueurM(),
                            p.getLargeurM(),
                            p.getTypePorte(),
                            p.getPriseElectrique()
                    );
                }
            }
        }

        // Propriétaire principal
        ProprietaireBienResponse proprio = proprietaireBienRepository
                .findFirstByBienIdAndEnabledTrueOrderByIdAsc(unite.getId())
                .map(lien -> new ProprietaireBienResponse(
                        lien.getProprietaireType(),
                        lien.getProprietaireNom(),
                        lien.getProprietairePrenom(),
                        lien.getProprietaireEmail(),
                        lien.getProprietaireEntrepriseNom()
                ))
                .orElse(null);

        return new BienResponse(
                unite.getId(),
                unite.getNomReference(),
                unite.getTypeBien(),
                unite.getSousType(),
                unite.getLibelleUnite(),
                unite.getCodePublic() != null ? unite.getCodePublic().toString() : null,

                rue,
                numero,
                boiteAdresse,
                boiteUnite,
                codePostal,
                ville,
                pays,

                parent != null ? parent.getId() : null,
                parent != null ? parent.getNomReference() : null,
                parent != null ? parent.getLibelleUnite() : null,

                residentiel,
                commerce,
                bureau,
                parking,
                proprio
        );
    }
}
