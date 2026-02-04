package com.locapro.backend.mapper;

import com.locapro.backend.dto.bien.BienCreationRequest;
import com.locapro.backend.dto.bien.BienInfosDeBaseRequest;
import com.locapro.backend.dto.bien.BienParentInfosRequest;
import com.locapro.backend.entity.BienEntity;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BienEntityMapper {

    // ========================================================================
    // CRÉATION : BienCreationRequest → BienEntity
    // ========================================================================

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "nomReference", source = "nomReferenceInterne")
    @Mapping(target = "libelleUnite", source = "libelleVisible")
    @Mapping(target = "typeBien", expression = "java(req.typeBien() != null ? req.typeBien().name() : null)")
    @Mapping(target = "sousType", expression = "java(req.sousType() != null ? req.sousType().name() : null)")
    BienEntity toEntity(BienCreationRequest req);

    @AfterMapping
    default void applyDefaults(@MappingTarget BienEntity entity) {
        entity.setStatut("ACTIF");
        entity.setEnabled(true);
        // Note: Le fallback libelleUnite → nomReference généré est géré dans BienServiceImpl
        // après la génération du nouveau nomReference auto-incrémenté
    }

    // ========================================================================
    // UPDATE UNITÉ (null-safe, pour mise à jour partielle)
    // ========================================================================

    default void updateUnite(BienEntity entity, BienInfosDeBaseRequest req) {
        if (entity == null || req == null) return;
        // Note: nomReference est IMMUABLE (trigger BDD), on ne modifie que libelleUnite
        if (req.libelleVisible() != null) entity.setLibelleUnite(req.libelleVisible());
        if (req.numeroPorte() != null) entity.setNumeroPorte(req.numeroPorte());
        if (req.boiteUnite() != null) entity.setBoiteUnite(req.boiteUnite());
        if (req.estUniteLocative() != null) entity.setEstUniteLocative(req.estUniteLocative());
        if (req.description() != null) entity.setDescription(req.description());
        if (req.revenuCadastral() != null) entity.setRevenuCadastral(req.revenuCadastral());
        if (req.rue() != null) entity.setRue(req.rue());
        if (req.numero() != null) entity.setNumero(req.numero());
        if (req.boite() != null) entity.setBoite(req.boite());
        if (req.codePostal() != null) entity.setCodePostal(req.codePostal());
        if (req.ville() != null) entity.setVille(req.ville());
        if (req.commune() != null) entity.setCommune(req.commune());
        if (req.pays() != null) entity.setPays(req.pays());
    }

    // ========================================================================
    // UPDATE PARENT (null-safe, pour mise à jour partielle)
    // ========================================================================

    default void updateParent(BienEntity parent, BienParentInfosRequest req) {
        if (parent == null || req == null) return;
        if (req.libelleVisible() != null) parent.setLibelleUnite(req.libelleVisible());
        if (req.rue() != null) parent.setRue(req.rue());
        if (req.numero() != null) parent.setNumero(req.numero());
        if (req.boite() != null) parent.setBoite(req.boite());
        if (req.codePostal() != null) parent.setCodePostal(req.codePostal());
        if (req.ville() != null) parent.setVille(req.ville());
        if (req.commune() != null) parent.setCommune(req.commune());
        if (req.pays() != null) parent.setPays(req.pays());
        if (req.latitude() != null) parent.setLatitude(req.latitude());
        if (req.longitude() != null) parent.setLongitude(req.longitude());
    }
}
