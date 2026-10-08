import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api } from '../api/client'
import { AuthContext } from './contexto'
import { clearToken, getToken, setToken, tokenExpirado } from './token'
import type { AuthResponse, Usuario } from './types'

function hayTokenValido(): boolean {
  const token = getToken()
  return token !== null && !tokenExpirado(token)
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [cargando, setCargando] = useState(hayTokenValido)

  const logout = useCallback(() => {
    clearToken()
    setUsuario(null)
  }, [])

  const refrescarPerfil = useCallback(async () => {
    const { data } = await api.get<Usuario>('/api/auth/me')
    setUsuario(data)
  }, [])

  useEffect(() => {
    if (!hayTokenValido()) {
      clearToken()
      return
    }
    let activo = true
    api
      .get<Usuario>('/api/auth/me')
      .then((respuesta) => {
        if (activo) setUsuario(respuesta.data)
      })
      .catch(() => {
        if (activo) logout()
      })
      .finally(() => {
        if (activo) setCargando(false)
      })
    return () => {
      activo = false
    }
  }, [logout])

  useEffect(() => {
    window.addEventListener('auth:expirada', logout)
    return () => window.removeEventListener('auth:expirada', logout)
  }, [logout])

  const login = useCallback(async (email: string, password: string) => {
    const { data } = await api.post<AuthResponse>('/api/auth/login', { email, password })
    setToken(data.token)
    setUsuario(data.usuario)
  }, [])

  const loginConToken = useCallback(
    async (token: string) => {
      setToken(token)
      try {
        await refrescarPerfil()
      } catch (error) {
        logout()
        throw error
      }
    },
    [refrescarPerfil, logout],
  )

  const valor = useMemo(
    () => ({ usuario, cargando, login, loginConToken, logout }),
    [usuario, cargando, login, loginConToken, logout],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}
