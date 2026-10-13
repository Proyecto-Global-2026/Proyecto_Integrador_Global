export type DecisionRevision = 'APROBADA' | 'RECHAZADA' | 'AJUSTES_SOLICITADOS'

export interface RevisionRequest {
  estado: DecisionRevision
  comentario?: string
}

export interface Revision {
  id: string
  planeacionId: string
  estadoAnterior: string
  estadoNuevo: string
  comentario: string | null
  revisadoPorId: string
  revisadoPorNombre: string
  createdAt: string
}