import { api } from './client'
import type { Avance, AvanceRequest } from '../types/avances'

export async function listarAvances(planeacionId: string): Promise<Avance[]> {
  const respuesta = await api.get<Avance[]>(`/api/planeaciones/${planeacionId}/avances`)
  return respuesta.data
}

export async function registrarAvance(
  planeacionId: string,
  payload: AvanceRequest,
): Promise<Avance> {
  const respuesta = await api.post<Avance>(`/api/planeaciones/${planeacionId}/avances`, payload)
  return respuesta.data
}

export async function actualizarAvance(
  planeacionId: string,
  avanceId: string,
  payload: AvanceRequest,
): Promise<Avance> {
  const respuesta = await api.put<Avance>(
    `/api/planeaciones/${planeacionId}/avances/${avanceId}`,
    payload,
  )
  return respuesta.data
}

export async function eliminarAvance(planeacionId: string, avanceId: string): Promise<void> {
  await api.delete(`/api/planeaciones/${planeacionId}/avances/${avanceId}`)
}