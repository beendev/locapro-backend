package com.locapro.backend.dto.bien;

import java.util.List;

public record BienArborescenceResponse(
        BienResponse unite,
        List<BienResponse> parents,
        List<BienResponse> sousBiens  // Enfants de ce bien (si c'est un bâtiment)
) {}