import { api } from './client'
import type { Paginado } from '../types/comun'
import type {
  EstadoPlaneacion,
  Planeacion,
  PlaneacionCreateRequest,
  PlaneacionUpdateRequest,
} from '../types/planeaciones'

interface ParamsLista {
  q?: string | null
  estado?: EstadoPlaneacion | null
  materiaId?: string | null
  page?: number
  size?: number
  sort?: string
}

export async function listarPlaneaciones(
  params: ParamsLista = {},
): Promise<Paginado<Planeacion>> {
  const respuesta = await api.get<Paginado<Planeacion>>('/api/planeaciones', {
    params: {
      q: params.q || undefined,
      estado: params.estado || undefined,
      materiaId: params.materiaId || undefined,
      page: params.page ?? 0,
      size: params.size ?? 20,
      sort: params.sort ?? 'createdAt,desc',
    },
  })
  return respuesta.data
}

export async function crearPlaneacion(
  payload: PlaneacionCreateRequest,
): Promise<Planeacion> {
  const respuesta = await api.post<Planeacion>('/api/planeaciones', payload)
  return respuesta.data
}

export async function actualizarPlaneacion(
  id: string,
  payload: PlaneacionUpdateRequest,
): Promise<Planeacion> {
  const respuesta = await api.put<Planeacion>(`/api/planeaciones/${id}`, payload)
  return respuesta.data
}