export interface Evidencia {
  id: string
  usuarioId: string
  usuarioNombre: string
  curso: string
  institucion: string
  fecha: string
  horasAcreditadas: number
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
