package com.locapro.backend.dto.bien;

public record DetailsCommerceResponse (
    Double surfaceCommercialeM2,
    Double surfaceVitrineM2,
    Double surfaceReserveM2,
    Boolean extractionHoreca
)
{}

