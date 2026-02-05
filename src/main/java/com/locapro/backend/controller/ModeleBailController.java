package com.locapro.backend.controller;

import com.locapro.backend.domain.context.LangueContrat;
import com.locapro.backend.domain.context.RegionBail;
import com.locapro.backend.domain.context.TypeContratBail;
import com.locapro.backend.dto.bail.ModeleBailResponse;
import com.locapro.backend.service.ModeleBailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/modeles-baux")
@Tag(name = "Modèles de Baux", description = "Gestion des templates Word")
public class ModeleBailController {

    private final ModeleBailService modeleBailService;

    public ModeleBailController(ModeleBailService modeleBailService) {
        this.modeleBailService = modeleBailService;
    }

    @GetMapping
    public ResponseEntity<List<ModeleBailResponse>> getAll() {
        return ResponseEntity.ok(modeleBailService.getAllModeles());
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Uploader un nouveau template Word (.docx)")
    public ResponseEntity<ModeleBailResponse> upload(
            @RequestParam("fichier") MultipartFile fichier,
            @RequestParam("region") RegionBail region,
            @RequestParam("typeContrat") TypeContratBail typeContrat,
            @RequestParam("langue") LangueContrat langue,
            @RequestParam("version") String version
    ) throws IOException {
        return ResponseEntity.ok(
                modeleBailService.uploadModele(fichier, region, typeContrat, langue, version)
        );
    }
}