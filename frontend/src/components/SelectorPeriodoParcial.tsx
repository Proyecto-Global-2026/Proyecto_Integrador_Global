import { FormControl, InputLabel, MenuItem, Select, Stack } from '@mui/material'
import { useEffect, useState } from 'react'
import { listarParciales, listarPeriodos } from '../api/catalogo'
import type { Parcial, Periodo } from '../types/catalogo'

interface Props {
  periodoId: string
  parcialId: string
  onPeriodoChange: (periodoId: string) => void
  onParcialChange: (parcialId: string) => void
  disabled?: boolean
}

export function SelectorPeriodoParcial({
  periodoId,
  parcialId,
  onPeriodoChange,
  onParcialChange,
  disabled = false,
}: Props) {
  const [periodos, setPeriodos] = useState<Periodo[]>([])
  const [parciales, setParciales] = useState<Parcial[]>([])

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      try {
        const datos = await listarPeriodos({ activo: true, size: 100, sort: 'fechaInicio,desc' })
        if (!cancelado) setPeriodos(datos.content)
      } catch {
        if (!cancelado) setPeriodos([])
      }
    }
    void ejecutar()
    return () => {
      cancelado = true
    }
  }, [])

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      if (!periodoId) {
        if (!cancelado) setParciales([])
        return
      }
      try {
        const datos = await listarParciales({ periodoId, activo: true, size: 100 })
        if (!cancelado) setParciales(datos.content)
      } catch {
        if (!cancelado) setParciales([])
      }
    }
    void ejecutar()
    return () => {
      cancelado = true
    }
  }, [periodoId])

  return (
    <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
      <FormControl fullWidth disabled={disabled}>
        <InputLabel>Periodo</InputLabel>
        <Select
          value={periodoId}
          label="Periodo"
          onChange={(e) => {
            onPeriodoChange(e.target.value)
            onParcialChange('')
          }}
        >
          {periodos.map((p) => (
            <MenuItem key={p.id} value={p.id}>
              {p.nombre}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
      <FormControl fullWidth disabled={disabled || !periodoId}>
        <InputLabel>Parcial</InputLabel>
        <Select value={parcialId} label="Parcial" onChange={(e) => onParcialChange(e.target.value)}>
          {parciales.map((p) => (
            <MenuItem key={p.id} value={p.id}>
              {p.nombre}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    </Stack>
  )
}
