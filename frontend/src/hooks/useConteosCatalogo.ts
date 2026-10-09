import { useEffect, useState } from 'react'
import type { Paginado } from '../types/comun'

type ListarFn = (params: {
  activo: boolean
  page: number
  size: number
}) => Promise<Paginado<unknown>>

export function useConteosCatalogo(listar: ListarFn, version: number) {
  const [activos, setActivos] = useState(0)
  const [inactivos, setInactivos] = useState(0)

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      try {
        const [a, i] = await Promise.all([
          listar({ activo: true, page: 0, size: 1 }),
          listar({ activo: false, page: 0, size: 1 }),
        ])
        if (!cancelado) {
          setActivos(a.totalElementos)
          setInactivos(i.totalElementos)
        }
      } catch {
        if (!cancelado) {
          setActivos(0)
          setInactivos(0)
        }
      }
    }
    void ejecutar()
    return () => {
      cancelado = true
    }
  }, [listar, version])

  return { activos, inactivos }
}
