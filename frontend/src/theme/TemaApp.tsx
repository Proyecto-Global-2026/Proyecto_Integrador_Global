import CssBaseline from '@mui/material/CssBaseline'
import { ThemeProvider } from '@mui/material/styles'
import { useEffect, useMemo, useState } from 'react'
import type { ReactNode } from 'react'
import { CONTEXTO_TEMA } from './contextoTema'
import { crearTema, type Modo } from './index'

const CLAVE_ALMACEN = 'tema-app'

function leerModoInicial(): Modo {
  try {
    const guardado = window.localStorage.getItem(CLAVE_ALMACEN)
    return guardado === 'light' ? 'light' : 'dark'
  } catch {
    return 'dark'
  }
}

export function TemaApp({ children }: { children: ReactNode }) {
  const [modo, setModo] = useState<Modo>(leerModoInicial)

  useEffect(() => {
    try {
      window.localStorage.setItem(CLAVE_ALMACEN, modo)
    } catch {
      // almacenamiento no disponible: se ignora
    }
  }, [modo])

  const tema = useMemo(() => crearTema(modo), [modo])
  const valor = useMemo(
    () => ({
      modo,
      alternarModo: () => setModo((actual) => (actual === 'dark' ? 'light' : 'dark')),
    }),
    [modo],
  )

  return (
    <CONTEXTO_TEMA.Provider value={valor}>
      <ThemeProvider theme={tema}>
        <CssBaseline />
        {children}
      </ThemeProvider>
    </CONTEXTO_TEMA.Provider>
  )
}