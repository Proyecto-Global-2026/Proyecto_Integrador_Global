package com.example.proyecto.global.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.proyecto.global.domain.SolicitudArco;

public interface SolicitudArcoRepository extends JpaRepository<SolicitudArco, UUID> {

	List<SolicitudArco> findByUsuarioIdOrderByFechaSolicitudDesc(UUID usuarioId);
}
