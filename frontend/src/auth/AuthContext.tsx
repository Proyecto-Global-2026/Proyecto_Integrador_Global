import { useCallback, useEffect, useMemo, useState, type ReactNode } from 'react'
import { api } from '../api/client'
import { AuthContext } from './contexto'
import { clearToken, expiracionDelToken, getToken, setToken, tokenExpirado } from './token'
import type { AuthResponse, Usuario } from './types'

function hayTokenValido(): boolean {
  const token = getToken()
  return token !== null && !tokenExpirado(token)
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [cargando, setCargando] = useState(hayTokenValido)
  const [sesionExpirada, setSesionExpirada] = useState(false)

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

  // Cierra la sesion en el instante exacto en que el JWT expira, sin esperar a
  // que una peticion falle con 401. Se reprograma en cada inicio de sesion.
  useEffect(() => {
    if (!usuario) return
    const token = getToken()
    if (!token) return
    const expiracion = expiracionDelToken(token)
    if (expiracion === null) return
    const retraso = Math.min(Math.max(expiracion - Date.now(), 0), 2 ** 31 - 1)
    const temporizador = window.setTimeout(() => {
      setSesionExpirada(true)
      logout()
    }, retraso)
    return () => window.clearTimeout(temporizador)
  }, [usuario, logout])

  useEffect(() => {
    const alExpirar = () => {
      setSesionExpirada(true)
      logout()
    }
    window.addEventListener('auth:expirada', alExpirar)
    return () => window.removeEventListener('auth:expirada', alExpirar)
  }, [logout])

  const login = useCallback(async (email: string, password: string) => {
    const { data } = await api.post<AuthResponse>('/api/auth/login', { email, password })
    setToken(data.token)
    setSesionExpirada(false)
    setUsuario(data.usuario)
  }, [])

  const loginConToken = useCallback(
    async (token: string) => {
      setToken(token)
      setSesionExpirada(false)
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
    () => ({ usuario, cargando, sesionExpirada, login, loginConToken, logout }),
    [usuario, cargando, sesionExpirada, login, loginConToken, logout],
  )

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>
}
