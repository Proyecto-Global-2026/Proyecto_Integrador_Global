package com.example.proyecto.global.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.global.domain.AuditoriaEvento;
import com.example.proyecto.global.dto.AuditoriaFiltroRequest;
import com.example.proyecto.global.service.AuditoriaService;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditoriaEvento>> listar(AuditoriaFiltroRequest filtro, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(auditoriaService.listar(filtro, pageable));
    }
}
