import { api } from './client'
import type {
  Paginado,
  Rol,
  Usuario,
  UsuarioCreateRequest,
  UsuarioUpdateRequest,
} from '../types/usuario'

export async function listarUsuarios(params: {
  rol?: string | null
  activo?: boolean | null
  page?: number
  size?: number
  sort?: string
} = {}): Promise<Paginado<Usuario>> {
  const respuesta = await api.get<Paginado<Usuario>>('/api/usuarios', {
    params: {
      rol: params.rol || undefined,
      activo: params.activo === null ? undefined : params.activo,
      page: params.page ?? 0,
      size: params.size ?? 20,
      sort: params.sort ?? 'nombre',
    },
  })
  return respuesta.data
}

export async function crearUsuario(payload: UsuarioCreateRequest): Promise<Usuario> {
  const respuesta = await api.post<Usuario>('/api/usuarios', payload)
  return respuesta.data
}

export async function actualizarUsuario(id: string, payload: UsuarioUpdateRequest): Promise<Usuario> {
  const respuesta = await api.put<Usuario>(`/api/usuarios/${id}`, payload)
  return respuesta.data
}

export async function desactivarUsuario(id: string): Promise<void> {
  await api.delete(`/api/usuarios/${id}`)
}

export async function reactivarUsuario(id: string): Promise<Usuario> {
  const respuesta = await api.post<Usuario>(`/api/usuarios/${id}/reactivar`)
  return respuesta.data
}

export async function listarRoles(): Promise<Rol[]> {
  const respuesta = await api.get<Rol[]>('/api/roles')
  return respuesta.data
}
