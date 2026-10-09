import { createContext, useContext } from 'react'
import type { Modo } from './index'

export interface ContextoDeTema {
  modo: Modo
  alternarModo: () => void
}

export const CONTEXTO_TEMA = createContext<ContextoDeTema | null>(null)

export function useTema(): ContextoDeTema {
  const valor = useContext(CONTEXTO_TEMA)
  if (!valor) {
    throw new Error('useTema debe usarse dentro de TemaApp')
  }
  return valor
}