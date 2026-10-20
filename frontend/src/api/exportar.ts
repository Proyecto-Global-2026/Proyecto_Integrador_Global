import { api } from './client'

export async function descargarReportePdf(tipo: 'planeaciones' | 'avances' | 'cursos', periodoId?: string | null): Promise<void> {
  const response = await api.get(`/api/reportes/${tipo}/pdf`, {
    params: { periodoId: periodoId || undefined },
    responseType: 'blob',
  })
  const blob = new Blob([response.data], { type: 'application/pdf' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `reporte-${tipo}.pdf`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}

export async function descargarReporteExcel(tipo: 'planeaciones' | 'avances' | 'cursos', periodoId?: string | null): Promise<void> {
  const response = await api.get(`/api/reportes/${tipo}/excel`, {
    params: { periodoId: periodoId || undefined },
    responseType: 'blob',
  })
  const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  const url = window.URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `reporte-${tipo}.xlsx`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  window.URL.revokeObjectURL(url)
}
