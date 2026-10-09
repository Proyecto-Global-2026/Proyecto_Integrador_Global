import { api } from './client'
import type { ArchivoPlaneacion } from '../types/archivos'

export const FORMATOS_PERMITIDOS = [
  'pdf',
  'doc',
  'docx',
  'xls',
  'xlsx',
  'ppt',
  'pptx',
  'jpg',
  'jpeg',
  'png',
] as const

export const ACCEPT_FORMATOS = FORMATOS_PERMITIDOS.map((formato) => `.${formato}`).join(',')

export const MAXIMO_MB_ARCHIVO = 10

export function extensionDeNombre(nombre: string): string {
  const punto = nombre.lastIndexOf('.')
  return punto === -1 ? '' : nombre.slice(punto + 1).toLowerCase()
}

export function validarArchivo(archivo: File): string | null {
  const extension = extensionDeNombre(archivo.name)
  if (!FORMATOS_PERMITIDOS.includes(extension as (typeof FORMATOS_PERMITIDOS)[number])) {
    return `Tipo de archivo no permitido (.${extension || 'sin extension'}). Formatos admitidos: ${FORMATOS_PERMITIDOS.join(', ')}`
  }
  if (archivo.size === 0) {
    return 'El archivo esta vacio'
  }
  const maximoBytes = MAXIMO_MB_ARCHIVO * 1024 * 1024
  if (archivo.size > maximoBytes) {
    return `El archivo excede el tamano maximo de ${MAXIMO_MB_ARCHIVO} MB`
  }
  return null
}

export async function listarArchivos(planeacionId: string): Promise<ArchivoPlaneacion[]> {
  const respuesta = await api.get<ArchivoPlaneacion[]>(`/api/planeaciones/${planeacionId}/archivos`)
  return respuesta.data
}

export async function subirArchivo(planeacionId: string, archivo: File): Promise<ArchivoPlaneacion> {
  const formData = new FormData()
  formData.append('archivo', archivo)
  const respuesta = await api.post<ArchivoPlaneacion>(
    `/api/planeaciones/${planeacionId}/archivos`,
    formData,
  )
  return respuesta.data
}

export async function eliminarArchivo(planeacionId: string, archivoId: string): Promise<void> {
  await api.delete(`/api/planeaciones/${planeacionId}/archivos/${archivoId}`)
}

export async function descargarArchivo(planeacionId: string, archivo: ArchivoPlaneacion): Promise<void> {
  const respuesta = await api.get<Blob>(`/api/planeaciones/${planeacionId}/archivos/${archivo.id}`, {
    responseType: 'blob',
  })
  const url = window.URL.createObjectURL(respuesta.data)
  const enlace = document.createElement('a')
  enlace.href = url
  enlace.download = archivo.nombreOriginal
  document.body.appendChild(enlace)
  enlace.click()
  enlace.remove()
  window.URL.revokeObjectURL(url)
}