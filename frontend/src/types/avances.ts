export type EstadoAvance = 'CUMPLIDO' | 'PARCIAL' | 'NO_CUMPLIDO'

export const ESTADOS_AVANCE: EstadoAvance[] = ['CUMPLIDO', 'PARCIAL', 'NO_CUMPLIDO']

export const ETIQUETAS_AVANCE: Record<EstadoAvance, string> = {
  CUMPLIDO: 'Cumplido',
  PARCIAL: 'Parcial',
  NO_CUMPLIDO: 'No cumplido',
}

export interface Avance {
  id: string
  planeacionId: string
  planeacionTitulo: string
  parcialId: string
  parcialNombre: string
  estado: EstadoAvance
  observaciones: string | null
  registradoPorId: string
  registradoPorNombre: string
  createdAt: string
  updatedAt: string
}

export interface AvanceRequest {
  estado: EstadoAvance
  observaciones?: string
}