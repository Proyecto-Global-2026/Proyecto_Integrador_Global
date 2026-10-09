export type EstadoPlaneacion = 'PENDIENTE' | 'AJUSTES_SOLICITADOS' | 'APROBADA' | 'RECHAZADA'

export const ESTADOS_PLANEACION: EstadoPlaneacion[] = [
  'PENDIENTE',
  'AJUSTES_SOLICITADOS',
  'APROBADA',
  'RECHAZADA',
]

export interface Planeacion {
  id: string
  docenteId: string
  docenteNombre: string
  materiaId: string
  materiaNombre: string
  materiaCodigo: string
  parcialId: string
  parcialNombre: string
  titulo: string
  contenido: string
  estado: EstadoPlaneacion
  createdAt: string
  updatedAt: string
}

export interface PlaneacionCreateRequest {
  materiaId: string
  parcialId: string
  titulo: string
  contenido: string
}

export type PlaneacionUpdateRequest = PlaneacionCreateRequest