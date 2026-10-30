package com.example.proyecto.global.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AuditoriaFiltroRequest(
        UUID usuarioId,
        String rol,
        String recurso,
        String accion,
        String resultado,
        OffsetDateTime fechaDesde,
        OffsetDateTime fechaHasta
) {
}
