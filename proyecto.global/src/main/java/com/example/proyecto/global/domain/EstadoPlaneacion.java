package com.example.proyecto.global.domain;

/**
 * Estados del ciclo de vida de una planeacion.
 * PENDIENTE: recien creada por el docente.
 * AJUSTES_SOLICITADOS: el coordinador pidio cambios; el docente puede
 * editarla y volver a enviar (queda PENDIENTE al editar).
 * APROBADA / RECHAZADA: fin del ciclo, ya no se edita.
 */
public enum EstadoPlaneacion {
	PENDIENTE,
	AJUSTES_SOLICITADOS,
	APROBADA,
	RECHAZADA
}