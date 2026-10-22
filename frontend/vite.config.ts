import react from '@vitejs/plugin-react'
import { readFileSync } from 'node:fs'
import type { Plugin } from 'vite'
import { defineConfig, loadEnv } from 'vite'

// Politica de seguridad de contenido aplicada solo al build de produccion.
// En desarrollo Vite inyecta scripts en linea para el HMR, por eso no se
// aplica ahi; el servidor de desarrollo solo agrega las cabeceras basicas.
const CSP_PRODUCCION = [
  "default-src 'self'",
  "script-src 'self'",
  "style-src 'self' 'unsafe-inline' https://fonts.googleapis.com",
  "font-src 'self' data: https://fonts.gstatic.com",
  "img-src 'self' data: blob: https: http://localhost:8080 http://127.0.0.1:8080",
  "connect-src 'self' https: http://localhost:8080 http://127.0.0.1:8080 ws://localhost:5173 ws://127.0.0.1:5173",
  "object-src 'none'",
  "base-uri 'self'",
  "form-action 'self'",
  "frame-ancestors 'none'",
].join('; ')

function cspEnProduccion(): Plugin {
  return {
    name: 'csp-en-produccion',
    apply: 'build',
    transformIndexHtml(html) {
      return {
        html,
        tags: [
          {
            tag: 'meta',
            attrs: {
              'http-equiv': 'Content-Security-Policy',
              content: CSP_PRODUCCION,
            },
            injectTo: 'head-prepend',
          },
        ],
      }
    },
  }
}

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), 'VITE_')

  // HTTPS en el servidor de desarrollo: opcional, se activa con
  // VITE_HTTPS=true y las rutas del certificado y la llave.
  const httpsHabilitado = env.VITE_HTTPS === 'true'
  const rutaCertificado = env.VITE_HTTPS_CERT
  const rutaLlave = env.VITE_HTTPS_KEY
  const tls =
    httpsHabilitado && rutaCertificado && rutaLlave
      ? { cert: readFileSync(rutaCertificado), key: readFileSync(rutaLlave) }
      : undefined

  const cabecerasSeguridad: Record<string, string> = {
    'X-Content-Type-Options': 'nosniff',
    'X-Frame-Options': 'DENY',
    'Referrer-Policy': 'strict-origin-when-cross-origin',
    'Permissions-Policy': 'camera=(), microphone=(), geolocation=()',
  }

  if (tls) {
    cabecerasSeguridad['Strict-Transport-Security'] = 'max-age=31536000; includeSubDomains'
  }

  return {
    plugins: [react(), cspEnProduccion()],
    server: {
      host: '127.0.0.1',
      port: 5173,
      https: tls,
      headers: cabecerasSeguridad,
      hmr: {
        host: '127.0.0.1',
        port: 5173,
        protocol: tls ? 'wss' : 'ws',
      },
    },
    preview: {
      headers: cabecerasSeguridad,
    },
  }
})
