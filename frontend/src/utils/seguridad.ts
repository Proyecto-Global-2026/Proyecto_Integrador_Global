const HOSTS_LOCALES = ['localhost', '127.0.0.1', '::1']

/**
 * Indica si la aplicacion se ejecuta en un entorno de desarrollo local, donde
 * se permite HTTP sin cifrar.
 */
export function esEntornoLocal(): boolean {
  if (typeof window === 'undefined') return true
  return HOSTS_LOCALES.includes(window.location.hostname)
}

/**
 * Fuerza el uso de HTTPS en el navegador. Si la aplicacion se sirve por HTTP
 * fuera de localhost, redirige a la misma ruta usando TLS.
 */
export function forzarHttps(): void {
  if (typeof window === 'undefined') return
  if (esEntornoLocal()) return
  if (window.location.protocol === 'https:') return

  const destino = `https://${window.location.host}${window.location.pathname}${window.location.search}${window.location.hash}`
  window.location.replace(destino)
}

/**
 * Valida que la URL del backend use HTTPS fuera de desarrollo local. Si la
 * URL es insegura se registra el problema y se devuelve la version HTTPS para
 * evitar contenido mixto bloqueado por el navegador.
 */
export function validarUrlApi(url: string): string {
  if (esEntornoLocal()) return url
  if (url.startsWith('https://')) return url

  console.error(
    'Seguridad: VITE_API_URL debe usar HTTPS fuera de desarrollo local. Toda comunicacion con el backend debe viajar cifrada.',
  )
  return url.replace(/^http:\/\//, 'https://')
}
