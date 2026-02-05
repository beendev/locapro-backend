package com.locapro.backend.repository;

import com.locapro.backend.entity.PeriodeBailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface PeriodeBailRepository extends JpaRepository<PeriodeBailEntity, Long> {

    // Si besoin plus tard : récupérer uniquement les périodes actives d’un bail
    // List<PeriodeBailEntity> findByBailIdAndEnabledTrue(Long bailId);

    /**
     * Vérifie le chevauchement.
     * Correction par rapport à tes entités :
     * 1. On accède à 'bail' (relation dans PeriodeBailEntity)
     * 2. Puis on accède à 'bienId' (champ simple dans BailEntity)
     * -> Donc on écrit : p.bail.bienId
     */
    @Query("SELECT COUNT(p) > 0 FROM PeriodeBailEntity p " +
            "WHERE p.bail.bienId = :bienId " +
            "AND p.enabled = true " +
            "AND (" +
            "   (p.dateFin IS NULL OR p.dateFin >= :newStart) " +
            "   AND " +
            "   (p.dateDebut <= :newEnd)" +
            ")")
    boolean existsChevauchement(
            @Param("bienId") Long bienId,
            @Param("newStart") LocalDate newStart,
            @Param("newEnd") LocalDate newEnd
    );

    // 1. Pour récupérer la période active d'un bail
    Optional<PeriodeBailEntity> findFirstByBailId(Long bailId);

    // 2. Pour la suppression (Le fameux Delete)
    // Spring comprend "deleteBy" + "BailId" (le nom du champ)
    void deleteByBailId(Long bailId);

    // 3. Ta méthode existante pour les chevauchements (ne change pas)
    // @Query("...") ou méthode dérivée
    boolean existsByBienIdAndDateDebutLessThanEqualAndDateFinGreaterThanEqual(Long bienId, LocalDate fin, LocalDate debut);
}
