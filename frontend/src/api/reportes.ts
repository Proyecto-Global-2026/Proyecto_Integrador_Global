import { api } from './client'
import type { ReporteAvances, ReporteCursos, ReportePlaneaciones } from '../types/reportes'

export async function obtenerReportePlaneaciones(
  periodoId?: string | null,
): Promise<ReportePlaneaciones> {
  const respuesta = await api.get<ReportePlaneaciones>('/api/reportes/planeaciones', {
    params: { periodoId: periodoId || undefined },
  })
  return respuesta.data
}

export async function obtenerReporteAvances(periodoId?: string | null): Promise<ReporteAvances> {
  const respuesta = await api.get<ReporteAvances>('/api/reportes/avances', {
    params: { periodoId: periodoId || undefined },
  })
  return respuesta.data
}

export async function obtenerReporteCursos(periodoId?: string | null): Promise<ReporteCursos> {
  const respuesta = await api.get<ReporteCursos>('/api/reportes/cursos', {
    params: { periodoId: periodoId || undefined },
  })
  return respuesta.data
}
