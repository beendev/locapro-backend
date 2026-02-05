package com.locapro.backend.mapper;

import com.locapro.backend.dto.bail.ModeleBailResponse;
import com.locapro.backend.entity.ModeleBailEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ModeleBailMapper {

    // On transforme l'Enum en String pour le JSON
    @Mapping(target = "typeContrat", expression = "java(entity.getTypeContrat().name())")
    ModeleBailResponse toResponse(ModeleBailEntity entity);
}