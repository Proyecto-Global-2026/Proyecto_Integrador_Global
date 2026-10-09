import { api } from './client'
import type {
  Materia,
  MateriaCreateRequest,
  MateriaUpdateRequest,
  Paginado,
  Parcial,
  ParcialCreateRequest,
  ParcialUpdateRequest,
  Periodo,
  PeriodoCreateRequest,
  PeriodoUpdateRequest,
} from '../types/catalogo'

interface ParamsLista {
  q?: string | null
  activo?: boolean | null
  page?: number
  size?: number
  sort?: string
}

export async function listarMaterias(
  params: ParamsLista = {},
): Promise<Paginado<Materia>> {
  const respuesta = await api.get<Paginado<Materia>>('/api/materias', {
    params: {
      q: params.q || undefined,
      activo: params.activo === null ? undefined : params.activo,
      page: params.page ?? 0,
      size: params.size ?? 20,
      sort: params.sort ?? 'nombre',
    },
  })
  return respuesta.data
}

export async function crearMateria(payload: MateriaCreateRequest): Promise<Materia> {
  const respuesta = await api.post<Materia>('/api/materias', payload)
  return respuesta.data
}

export async function actualizarMateria(
  id: string,
  payload: MateriaUpdateRequest,
): Promise<Materia> {
  const respuesta = await api.put<Materia>(`/api/materias/${id}`, payload)
  return respuesta.data
}

export async function desactivarMateria(id: string): Promise<void> {
  await api.delete(`/api/materias/${id}`)
}

export async function reactivarMateria(id: string): Promise<Materia> {
  const respuesta = await api.post<Materia>(`/api/materias/${id}/reactivar`)
  return respuesta.data
}

export async function listarPeriodos(params: ParamsLista = {}): Promise<Paginado<Periodo>> {
  const respuesta = await api.get<Paginado<Periodo>>('/api/periodos', {
    params: {
      q: params.q || undefined,
      activo: params.activo === null ? undefined : params.activo,
      page: params.page ?? 0,
      size: params.size ?? 20,
      sort: params.sort ?? 'fechaInicio,desc',
    },
  })
  return respuesta.data
}

export async function crearPeriodo(payload: PeriodoCreateRequest): Promise<Periodo> {
  const respuesta = await api.post<Periodo>('/api/periodos', payload)
  return respuesta.data
}

export async function actualizarPeriodo(
  id: string,
  payload: PeriodoUpdateRequest,
): Promise<Periodo> {
  const respuesta = await api.put<Periodo>(`/api/periodos/${id}`, payload)
  return respuesta.data
}

export async function desactivarPeriodo(id: string): Promise<void> {
  await api.delete(`/api/periodos/${id}`)
}

export async function reactivarPeriodo(id: string): Promise<Periodo> {
  const respuesta = await api.post<Periodo>(`/api/periodos/${id}/reactivar`)
  return respuesta.data
}

export async function listarParciales(
  params: ParamsLista & { periodoId?: string | null } = {},
): Promise<Paginado<Parcial>> {
  const respuesta = await api.get<Paginado<Parcial>>('/api/parciales', {
    params: {
      periodoId: params.periodoId || undefined,
      q: params.q || undefined,
      activo: params.activo === null ? undefined : params.activo,
      page: params.page ?? 0,
      size: params.size ?? 20,
      sort: params.sort ?? 'fechaInicio,desc',
    },
  })
  return respuesta.data
}

export async function crearParcial(payload: ParcialCreateRequest): Promise<Parcial> {
  const respuesta = await api.post<Parcial>('/api/parciales', payload)
  return respuesta.data
}

export async function actualizarParcial(
  id: string,
  payload: ParcialUpdateRequest,
): Promise<Parcial> {
  const respuesta = await api.put<Parcial>(`/api/parciales/${id}`, payload)
  return respuesta.data
}

export async function desactivarParcial(id: string): Promise<void> {
  await api.delete(`/api/parciales/${id}`)
}

export async function reactivarParcial(id: string): Promise<Parcial> {
  const respuesta = await api.post<Parcial>(`/api/parciales/${id}/reactivar`)
  return respuesta.data
}
