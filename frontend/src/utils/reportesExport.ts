import { jsPDF } from 'jspdf'
import autoTable from 'jspdf-autotable'
import * as XLSX from 'xlsx'
import FileSaver from 'file-saver'

export type ReporteTipo = 'planeaciones' | 'avances' | 'cursos'

export interface FiltrosReporte {
  periodoId?: string | null
  periodoNombre?: string
  docenteId?: string | null
  materiaId?: string | null
  docenteNombre?: string
  materiaNombre?: string
}

export interface TablaReporte {
  titulo: string
  periodo: string
  encabezados: string[]
  filas: Array<Array<string | number>>
  resumen: string[]
  generado: string
}

export function exportarPdf(tabla: TablaReporte, nombreArchivo: string) {
  const doc = new jsPDF({
    orientation: 'landscape',
    unit: 'mm',
    format: 'a4'
  })

  doc.setFontSize(16)
  doc.text(tabla.titulo, 14, 14)

  doc.setFontSize(9)
  let y = 22
  doc.text(`Periodo: ${tabla.periodo}`, 14, y)
  y += 4
  doc.text(`Generado: ${tabla.generado}`, 14, y)
  y += 4

  tabla.resumen.forEach((r) => {
    y += 4
    if (y > 180) {
      doc.addPage('a4', 'landscape')
      y = 14
    }
    doc.text(r, 14, y)
  })

  y += 4

  autoTable(doc, {
    startY: y > 30 ? y + 2 : 30,
    head: [tabla.encabezados],
    body: tabla.filas,
    styles: {
      fontSize: 8,
      cellPadding: 2
    },
    headStyles: {
      fillColor: [51, 51, 51],
      textColor: [255, 255, 255]
    },
    alternateRowStyles: {
      fillColor: [245, 245, 245]
    },
    margin: { left: 14, right: 14 },
    tableWidth: 'auto'
  })

  doc.save(nombreArchivo.endsWith('.pdf') ? nombreArchivo : `${nombreArchivo}.pdf`)
}

export function exportarExcel(tabla: TablaReporte, nombreArchivo: string) {
  const wb = XLSX.utils.book_new()

  const wsData: (string | number | Date)[][] = []

  wsData.push([tabla.titulo])
  wsData.push([`Periodo: ${tabla.periodo}`])
  wsData.push([`Generado: ${tabla.generado}`])
  wsData.push([])
  tabla.resumen.forEach((r) => wsData.push([r]))
  wsData.push([])
  wsData.push(tabla.encabezados)
  tabla.filas.forEach((f) => wsData.push(f as (string | number)[]))

  const ws = XLSX.utils.aoa_to_sheet(wsData)

  const cols = tabla.encabezados.map(() => ({ wch: 20 }))
  const wsWithCols = ws as XLSX.WorkSheet & { '!cols'?: XLSX.ColInfo[] }
  wsWithCols['!cols'] = cols

  XLSX.utils.book_append_sheet(wb, ws, 'Reporte')
  const wbout = XLSX.write(wb, { bookType: 'xlsx', type: 'array' })
  const blob = new Blob([wbout], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' })
  FileSaver.saveAs(blob, nombreArchivo.endsWith('.xlsx') ? nombreArchivo : `${nombreArchivo}.xlsx`)
}
