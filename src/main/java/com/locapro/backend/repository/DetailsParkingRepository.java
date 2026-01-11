package com.locapro.backend.repository;

import com.locapro.backend.entity.DetailsParkingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DetailsParkingRepository extends JpaRepository<DetailsParkingEntity, Long> {
}
