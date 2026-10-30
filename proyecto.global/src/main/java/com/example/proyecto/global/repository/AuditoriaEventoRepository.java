package com.example.proyecto.global.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.proyecto.global.domain.AuditoriaEvento;

public interface AuditoriaEventoRepository extends JpaRepository<AuditoriaEvento, UUID>, JpaSpecificationExecutor<AuditoriaEvento> {
}
