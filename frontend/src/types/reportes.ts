export interface ReportePlaneaciones {
  total: number
  aprobadas: number
  rechazadas: number
  pendientes: number
  ajustesSolicitados: number
  porcentajeCumplimiento: number
}

export interface CumplimientoParcial {
  parcialId: string
  parcialNombre: string
  total: number
  cumplidos: number
  parciales: number
  noCumplidos: number
  porcentajeCumplimiento: number
}

export interface ReporteAvances {
  totalAvances: number
  porcentajeCumplimiento: number
  parciales: CumplimientoParcial[]
}

export interface CursosPorPeriodo {
  periodoId: string
  periodoNombre: string
  cursosAcreditados: number
  horasAcreditadas: number
}

export interface ReporteCursos {
  totalCursos: number
  totalHoras: number
  periodos: CursosPorPeriodo[]
}
