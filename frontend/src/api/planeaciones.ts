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
  parcialId?: string | null
  periodoId?: string | null
  page?: number
  size?: number
  sort?: string
}

interface ParamsHistorial extends ParamsLista {
  periodoId?: string | null
  docenteId?: string | null
  parcialId?: string | null
}

export async function listarPlaneaciones(
  params: ParamsLista = {},
): Promise<Paginado<Planeacion>> {
  const respuesta = await api.get<Paginado<Planeacion>>('/api/planeaciones', {
    params: {
      q: params.q || undefined,
      estado: params.estado || undefined,
      materiaId: params.materiaId || undefined,
      parcialId: params.parcialId || undefined,
      periodoId: params.periodoId || undefined,
      page: params.page ?? 0,
      size: params.size ?? 20,
      sort: params.sort ?? 'createdAt,desc',
    },
  })
  return respuesta.data
}

export async function listarPlaneacionesConsolidado(
  periodoId?: string | null,
  size: number = 1000,
): Promise<Planeacion[]> {
  const params: ParamsLista = {
    periodoId: periodoId || null,
    page: 0,
    size,
    sort: 'createdAt,desc',
  }
  const respuesta = await api.get<Paginado<Planeacion>>('/api/planeaciones', {
    params: {
      q: params.q || undefined,
      estado: params.estado || undefined,
      materiaId: params.materiaId || undefined,
      parcialId: params.parcialId || undefined,
      periodoId: params.periodoId || undefined,
      page: params.page ?? 0,
      size: params.size ?? size,
      sort: params.sort ?? 'createdAt,desc',
    },
  })
  return respuesta.data.content
}

export async function listarHistorial(
  params: ParamsHistorial = {},
): Promise<Paginado<Planeacion>> {
  const respuesta = await api.get<Paginado<Planeacion>>('/api/planeaciones/historial', {
    params: {
      q: params.q || undefined,
      estado: params.estado || undefined,
      materiaId: params.materiaId || undefined,
      docenteId: params.docenteId || undefined,
      periodoId: params.periodoId || undefined,
      parcialId: params.parcialId || undefined,
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
