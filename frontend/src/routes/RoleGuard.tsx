import type { ReactNode } from 'react'
import { Navigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

interface Props {
  children: ReactNode
  roles?: string[]
}

export function RoleGuard({ children, roles }: Props) {
  const { usuario, cargando, sesionExpirada } = useAuth()
  if (cargando) return null
  if (!usuario) return <Navigate to="/login" replace state={{ sesionExpirada }} />
  if (roles && !roles.includes(usuario.rol)) return <Navigate to="/" replace />
  return children
}
