package com.reoagms.maintenance_service.repository;

import com.reoagms.maintenance_service.model.Technician;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TechnicianRepository extends JpaRepository<Technician, UUID> {
}
