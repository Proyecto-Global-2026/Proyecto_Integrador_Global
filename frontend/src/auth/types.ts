export interface Usuario {
  id: string
  nombre: string
  email: string
  rol: string
  activo: boolean
  proveedor: string | null
}

export interface AuthResponse {
  token: string
  tipo: string
  expiraEnSegundos: number
  usuario: Usuario
}

export interface ApiError {
  timestamp: string
  status: number
  error: string
  message: string
  path: string
  fieldErrors: Record<string, string>
  details: string[]
}
