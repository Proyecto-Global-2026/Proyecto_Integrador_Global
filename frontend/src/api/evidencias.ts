import { api } from './client'
import type { Paginado } from '../types/comun'
import type { Evidencia, EvidenciaForm } from '../types/evidencias'

interface ParamsLista {
  q?: string | null
  page?: number
  size?: number
  sort?: string
}

export async function listarEvidencias(params: ParamsLista = {}): Promise<Paginado<Evidencia>> {
  const respuesta = await api.get<Paginado<Evidencia>>('/api/evidencias', {
    params: {
      q: params.q || undefined,
      page: params.page ?? 0,
      size: params.size ?? 20,
      sort: params.sort ?? 'fecha,desc',
    },
  })
  return respuesta.data
}

function construirFormData(payload: EvidenciaForm, archivo?: File | null): FormData {
  const formData = new FormData()
  formData.append('curso', payload.curso)
  formData.append('institucion', payload.institucion)
  formData.append('fecha', payload.fecha)
  formData.append('horasAcreditadas', String(payload.horasAcreditadas))
  if (archivo) formData.append('archivo', archivo)
  return formData
}

export async function crearEvidencia(
  payload: EvidenciaForm,
  archivo?: File | null,
): Promise<Evidencia> {
  const respuesta = await api.post<Evidencia>('/api/evidencias', construirFormData(payload, archivo))
  return respuesta.data
}

export async function actualizarEvidencia(
  id: string,
  payload: EvidenciaForm,
  archivo?: File | null,
): Promise<Evidencia> {
  const respuesta = await api.put<Evidencia>(
    `/api/evidencias/${id}`,
    construirFormData(payload, archivo),
  )
  return respuesta.data
}

export async function eliminarEvidencia(id: string): Promise<void> {
  await api.delete(`/api/evidencias/${id}`)
}

export async function descargarEvidencia(evidencia: Evidencia): Promise<void> {
  const respuesta = await api.get<Blob>(`/api/evidencias/${evidencia.id}/archivo`, {
    responseType: 'blob',
  })
  const url = window.URL.createObjectURL(respuesta.data)
  const enlace = document.createElement('a')
  enlace.href = url
  enlace.download = evidencia.archivoNombre ?? `evidencia-${evidencia.id}`
  document.body.appendChild(enlace)
  enlace.click()
  enlace.remove()
  window.URL.revokeObjectURL(url)
}
