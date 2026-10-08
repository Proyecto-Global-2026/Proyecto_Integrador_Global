package com.example.proyecto.global.domain;

/**
 * Estados del ciclo de vida de una planeacion.
 * S1-09 solo deja la planeacion en PENDIENTE; la revision (aprobar/rechazar)
 * se implementa en la historia de revision.
 */
public enum EstadoPlaneacion {
	PENDIENTE,
	APROBADA,
	RECHAZADA
}