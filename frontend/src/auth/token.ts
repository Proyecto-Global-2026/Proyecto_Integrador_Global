const TOKEN_KEY = 'proyecto_global_token'

export function getToken(): string | null {
  return window.localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  window.localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  window.localStorage.removeItem(TOKEN_KEY)
}

/**
 * Devuelve el instante de expiracion del JWT en milisegundos (epoch), o null si
 * el payload no se puede leer. No verifica la firma (eso lo hace el backend).
 */
export function expiracionDelToken(token: string): number | null {
  const partes = token.split('.')
  if (partes.length !== 3) return null

  try {
    const payload = JSON.parse(
      decodeURIComponent(
        atob(partes[1].replace(/-/g, '+').replace(/_/g, '/'))
          .split('')
          .map((c) => '%' + c.charCodeAt(0).toString(16).padStart(2, '0'))
          .join(''),
      ),
    ) as { exp?: number }
    if (typeof payload.exp !== 'number') return null
    return payload.exp * 1000
  } catch {
    return null
  }
}

/**
 * Revisa la fecha de expiracion del JWT sin verificar la firma (eso lo hace el
 * backend). Si el payload no se puede leer se considera invalido.
 */
export function tokenExpirado(token: string): boolean {
  const exp = expiracionDelToken(token)
  if (exp === null) return true
  return exp <= Date.now()
}
