package com.reoagms.asset_service.facility.repository;

import com.reoagms.asset_service.facility.model.Facility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FacilityRepository extends JpaRepository<Facility, UUID> {
}