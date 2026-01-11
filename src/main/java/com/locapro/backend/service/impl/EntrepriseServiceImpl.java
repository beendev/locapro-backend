package com.locapro.backend.service.impl;

import com.locapro.backend.dto.agence.EntrepriseRequest;
import com.locapro.backend.dto.agence.EntrepriseResponse;
import com.locapro.backend.entity.EntrepriseEntity;
import com.locapro.backend.entity.UtilisateurEntity;
import com.locapro.backend.repository.EntrepriseRepository;
import com.locapro.backend.repository.UtilisateurRepository;
import com.locapro.backend.service.EntrepriseService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class EntrepriseServiceImpl implements EntrepriseService {

    private final EntrepriseRepository repo;
    private final UtilisateurRepository userRepo;

    public EntrepriseServiceImpl(EntrepriseRepository repo, UtilisateurRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    @Override
    public EntrepriseResponse createNewVersion(Long ownerUserId, EntrepriseRequest req) {
        if (!userRepo.existsById(ownerUserId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Utilisateur inconnu");
        }

        // désactiver l’ancienne entreprise (si existe)
        repo.disableCurrentForUser(ownerUserId);

        var e = new EntrepriseEntity();
        e.setProprietaire(userRepo.getReferenceById(ownerUserId));

        // Données de base
        e.setRaisonSociale(trimOrNull(req.raisonSociale()));
        e.setNumeroTva(trimOrNull(req.numeroTva()));

        // Email pro : si vide -> on prend l’email de l’utilisateur
        String emailPro = trimOrNull(req.emailPro());
        if (emailPro == null) {
            emailPro = userRepo.findById(ownerUserId)
                    .map(UtilisateurEntity::getEmail)
                    .orElse(null);
        }
        e.setEmailPro(emailPro);

        e.setTelephonePro(trimOrNull(req.telephonePro()));
        e.setIban(trimOrNull(req.iban()));
        e.setSiteWeb(trimOrNull(req.siteWeb()));

        // Adresse inline
        e.setRue(trimOrNull(req.rue()));
        e.setNumero(trimOrNull(req.numero()));
        e.setBoite(trimOrNull(req.boite()));
        e.setCodePostal(trimOrNull(req.codePostal()));
        e.setVille(trimOrNull(req.ville()));
        e.setPays(trimOrNull(req.pays()));
        e.setLatitude(req.latitude());
        e.setLongitude(req.longitude());

        e.setEnabled(true);

        try {
            var saved = repo.save(e);
            return toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            // par ex : contrainte UNIQUE sur numero_tva
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Conflit de contrainte (ex.: numéro TVA déjà utilisé)"
            );
        }
    }

    @Override
    @Transactional(readOnly = true)
    public EntrepriseResponse getCurrent(Long ownerUserId) {
        var e = repo.findByProprietaire_IdAndEnabledTrue(ownerUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Entreprise non définie"
                ));
        return toResponse(e);
    }

    @Override
    public EntrepriseResponse updateCurrent(Long ownerUserId, EntrepriseRequest req) {
        var e = repo.findByProprietaire_IdAndEnabledTrue(ownerUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Entreprise non définie"
                ));

        // Raison sociale
        if (req.raisonSociale() != null && !req.raisonSociale().trim().isEmpty()) {
            e.setRaisonSociale(req.raisonSociale().trim());
        }

        // TVA
        if (req.numeroTva() != null && !req.numeroTva().trim().isEmpty()) {
            e.setNumeroTva(req.numeroTva().trim());
        }

        // Email pro
        if (req.emailPro() != null && !req.emailPro().trim().isEmpty()) {
            e.setEmailPro(req.emailPro().trim());
        }

        // Téléphone pro
        if (req.telephonePro() != null && !req.telephonePro().trim().isEmpty()) {
            e.setTelephonePro(req.telephonePro().trim());
        }

        // IBAN
        if (req.iban() != null && !req.iban().trim().isEmpty()) {
            e.setIban(req.iban().trim());
        }

        // Site web
        if (req.siteWeb() != null && !req.siteWeb().trim().isEmpty()) {
            e.setSiteWeb(req.siteWeb().trim());
        }

        // Adresse : on met à jour seulement les champs non nulls
        if (req.rue() != null) {
            e.setRue(trimOrNull(req.rue()));
        }
        if (req.numero() != null) {
            e.setNumero(trimOrNull(req.numero()));
        }
        if (req.boite() != null) {
            e.setBoite(trimOrNull(req.boite()));
        }
        if (req.codePostal() != null) {
            e.setCodePostal(trimOrNull(req.codePostal()));
        }
        if (req.ville() != null) {
            e.setVille(trimOrNull(req.ville()));
        }
        if (req.pays() != null) {
            e.setPays(trimOrNull(req.pays()));
        }
        if (req.latitude() != null) {
            e.setLatitude(req.latitude());
        }
        if (req.longitude() != null) {
            e.setLongitude(req.longitude());
        }

        try {
            var saved = repo.save(e);
            return toResponse(saved);
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Conflit de contrainte (ex.: numéro TVA déjà utilisé)"
            );
        }
    }

    // ===== Helpers =====

    private String trimOrNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private EntrepriseResponse toResponse(EntrepriseEntity e) {
        return new EntrepriseResponse(
                e.getId(),
                e.getRaisonSociale(),
                e.getNumeroTva(),
                e.getEmailPro(),
                e.getTelephonePro(),
                e.getIban(),
                e.getSiteWeb(),
                e.getRue(),
                e.getNumero(),
                e.getBoite(),
                e.getCodePostal(),
                e.getVille(),
                e.getPays(),
                e.getLatitude(),
                e.getLongitude(),
                e.getEnabled()
        );
    }
}
