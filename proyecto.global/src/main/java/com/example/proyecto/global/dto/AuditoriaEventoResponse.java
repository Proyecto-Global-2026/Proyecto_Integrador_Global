package com.example.proyecto.global.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.proyecto.global.domain.AuditoriaEvento;

public record AuditoriaEventoResponse(
        UUID id,
        UUID usuarioId,
        String rol,
        String usuarioEmailMask,
        AuditoriaEvento.Accion accion,
        AuditoriaEvento.Resultado resultado,
        String recurso,
        UUID recursoId,
        String descripcion,
        String metodo,
        String ruta,
        String ip,
        String userAgent,
        OffsetDateTime fechaEvento
) {
    public static AuditoriaEventoResponse from(AuditoriaEvento evento) {
        return new AuditoriaEventoResponse(
                evento.getId(),
                evento.getUsuarioId(),
                evento.getRol(),
                evento.getUsuarioEmailMask(),
                evento.getAccion(),
                evento.getResultado(),
                evento.getRecurso(),
                evento.getRecursoId(),
                evento.getDescripcion(),
                evento.getMetodo(),
                evento.getRuta(),
                evento.getIp(),
                evento.getUserAgent(),
                evento.getFechaEvento()
        );
    }
}
