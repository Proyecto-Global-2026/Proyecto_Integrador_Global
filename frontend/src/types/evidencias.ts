export type EstadoEvidencia = 'PENDIENTE' | 'APROBADA' | 'RECHAZADA'

export type DecisionEvidencia = 'APROBADA' | 'RECHAZADA'

export const ESTADOS_EVIDENCIA: EstadoEvidencia[] = ['PENDIENTE', 'APROBADA', 'RECHAZADA']

export const ETIQUETAS_EVIDENCIA: Record<EstadoEvidencia, string> = {
  PENDIENTE: 'Pendiente',
  APROBADA: 'Aprobada',
  RECHAZADA: 'Rechazada',
}

export interface Evidencia {
  id: string
  usuarioId: string
  usuarioNombre: string
  curso: string
  institucion: string
  fecha: string
  horasAcreditadas: number
  estado: EstadoEvidencia
  tieneArchivo: boolean
  archivoNombre: string | null
  archivoContentType: string | null
  archivoTamano: number | null
  createdAt: string
  updatedAt: string
}

export interface EvidenciaForm {
  curso: string
  institucion: string
  fecha: string
  horasAcreditadas: number
}

export interface RevisionEvidencia {
  id: string
  evidenciaId: string
  estadoAnterior: string
  estadoNuevo: string
  comentario: string | null
  revisadoPorId: string
  revisadoPorNombre: string
  createdAt: string
}

export interface RevisionEvidenciaRequest {
  estado: DecisionEvidencia
  comentario?: string
}