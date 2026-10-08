export interface Rol {
  id: number
  nombre: string
  descripcion: string | null
}

export interface Usuario {
  id: string
  nombre: string
  email: string
  rol: string
  activo: boolean
  proveedor: string | null
}

export interface Paginado<T> {
  content: T[]
  pagina: number
  tamanoPagina: number
  totalElementos: number
  totalPaginas: number
}

export interface UsuarioCreateRequest {
  nombre: string
  email: string
  password: string
  rol: string
}

export interface UsuarioUpdateRequest {
  nombre: string
  email: string
  rol: string
}
