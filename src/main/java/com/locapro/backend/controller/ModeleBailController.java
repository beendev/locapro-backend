package com.locapro.backend.controller;

import com.locapro.backend.dto.bail.ModeleBailResponse;
import com.locapro.backend.domain.context.RegionBail;
import com.locapro.backend.domain.context.LangueContrat;
import com.locapro.backend.service.ModeleBailService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/modeles-bail")
public class ModeleBailController {

    private final ModeleBailService service;

    public ModeleBailController(ModeleBailService service) {
        this.service = service;
    }

    @GetMapping("/latest")
    public ModeleBailResponse getLatest(
            @RequestParam RegionBail regionBail,
            @RequestParam LangueContrat langue,
            @RequestParam String typeDocument
    ) {
        return service.getLatestActiveModel(regionBail, langue, typeDocument);
    }
}
