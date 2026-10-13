import { api } from './client'
import type { Revision, RevisionRequest } from '../types/revisiones'

export async function revisarPlaneacion(
  planeacionId: string,
  payload: RevisionRequest,
): Promise<Revision> {
  const respuesta = await api.post<Revision>(`/api/planeaciones/${planeacionId}/revisiones`, payload)
  return respuesta.data
}

export async function listarRevisiones(planeacionId: string): Promise<Revision[]> {
  const respuesta = await api.get<Revision[]>(`/api/planeaciones/${planeacionId}/revisiones`)
  return respuesta.data
}