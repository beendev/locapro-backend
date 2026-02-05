package com.locapro.backend.service.impl;

import com.locapro.backend.domain.context.LangueContrat;
import com.locapro.backend.domain.context.RegionBail;
import com.locapro.backend.domain.context.TypeContratBail;
import com.locapro.backend.dto.bail.ModeleBailResponse;
import com.locapro.backend.entity.ModeleBailEntity;
import com.locapro.backend.mapper.ModeleBailMapper;
import com.locapro.backend.repository.ModeleBailRepository;
import com.locapro.backend.service.ModeleBailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ModeleBailServiceImpl implements ModeleBailService {

    private final ModeleBailRepository modeleBailRepository;
    private final ModeleBailMapper modeleBailMapper; // Injection du Mapper

    // Dossier de stockage à la racine du projet (créé automatiquement)
    private static final String UPLOAD_DIR = "uploads/baux/";

    public ModeleBailServiceImpl(ModeleBailRepository modeleBailRepository, ModeleBailMapper modeleBailMapper) {
        this.modeleBailRepository = modeleBailRepository;
        this.modeleBailMapper = modeleBailMapper;
    }

    @Override
    public List<ModeleBailResponse> getAllModeles() {
        return modeleBailRepository.findAll().stream()
                .map(modeleBailMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ModeleBailResponse uploadModele(MultipartFile fichier, RegionBail region, TypeContratBail typeContrat, LangueContrat langue, String version) throws IOException {

        // 1. Création du dossier si inexistant
        File dossier = new File(UPLOAD_DIR);
        if (!dossier.exists()) {
            dossier.mkdirs();
        }

        // 2. Nommage unique du fichier
        String extension = ".docx";
        String nomFichier = String.format("%s_%s_%s_v%s_%d%s",
                region, typeContrat, langue, version, System.currentTimeMillis(), extension);

        Path cheminCible = Paths.get(UPLOAD_DIR + nomFichier);

        // 3. Écriture sur le disque
        Files.copy(fichier.getInputStream(), cheminCible, StandardCopyOption.REPLACE_EXISTING);

        // 4. Désactivation de l'ancien modèle (pour qu'il n'y ait qu'un seul actif par type)
        modeleBailRepository.findFirstByRegionBailAndTypeContratAndLangueAndActifBoolTrue(region, typeContrat, langue)
                .ifPresent(ancien -> {
                    ancien.setActifBool(false);
                    modeleBailRepository.save(ancien);
                });

        // 5. Sauvegarde en Base de Données
        ModeleBailEntity nouveau = new ModeleBailEntity();
        nouveau.setRegionBail(region);
        nouveau.setTypeContrat(typeContrat);
        nouveau.setLangue(langue);
        nouveau.setVersion(version);
        nouveau.setActifBool(true); // C'est le boss
        nouveau.setUrlFichier("file:" + cheminCible.toString()); // Préfixe "file:" important !

        // Note: creeLe est géré par @PrePersist dans l'entité, ou on le force ici
        nouveau.setCreeLe(OffsetDateTime.now());

        ModeleBailEntity saved = modeleBailRepository.save(nouveau);

        // 6. Retour via le Mapper
        return modeleBailMapper.toResponse(saved);
    }
}