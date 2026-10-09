import type { Paginado } from './comun'

export type { Paginado }

export interface Materia {
  id: string
  nombre: string
  codigo: string
  descripcion: string | null
  activo: boolean
  createdAt: string
  updatedAt: string
}

export interface MateriaCreateRequest {
  nombre: string
  codigo: string
  descripcion: string
}

export interface MateriaUpdateRequest {
  nombre: string
  codigo: string
  descripcion: string
  activo: boolean
}

export interface Periodo {
  id: string
  nombre: string
  fechaInicio: string
  fechaFin: string
  activo: boolean
  createdAt: string
  updatedAt: string
}

export interface PeriodoCreateRequest {
  nombre: string
  fechaInicio: string
  fechaFin: string
}

export interface PeriodoUpdateRequest {
  nombre: string
  fechaInicio: string
  fechaFin: string
  activo: boolean
}

export interface Parcial {
  id: string
  periodoId: string
  periodoNombre: string | null
  nombre: string
  fechaInicio: string
  fechaFin: string
  activo: boolean
  createdAt: string
  updatedAt: string
}

export interface ParcialCreateRequest {
  periodoId: string
  nombre: string
  fechaInicio: string
  fechaFin: string
}

export interface ParcialUpdateRequest {
  periodoId: string
  nombre: string
  fechaInicio: string
  fechaFin: string
  activo: boolean
}
