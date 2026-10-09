import Box from '@mui/material/Box'
import Paper from '@mui/material/Paper'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TablePagination from '@mui/material/TablePagination'
import TableRow from '@mui/material/TableRow'
import { alpha } from '@mui/material/styles'
import type { ReactNode } from 'react'
import { EsqueletoTabla } from './ui/EsqueletoTabla'
import { EstadoVacio } from './ui/EstadoVacio'

export interface ColumnaCatalogo<T> {
  encabezado: string
  alineacion?: 'left' | 'right' | 'center'
  render: (fila: T) => ReactNode
}

interface Props<T> {
  columnas: ColumnaCatalogo<T>[]
  filas: T[]
  cargando: boolean
  total: number
  pagina: number
  tamanoPagina: number
  onCambiarPagina: (pagina: number) => void
  onCambiarTamano: (tamano: number) => void
  claveFila: (fila: T) => string
  mensajeVacio?: string
  detalleVacio?: string
  acciones?: (fila: T) => ReactNode
}

export function TablaCatalogo<T>({
  columnas,
  filas,
  cargando,
  total,
  pagina,
  tamanoPagina,
  onCambiarPagina,
  onCambiarTamano,
  claveFila,
  mensajeVacio = 'No se encontraron registros',
  detalleVacio,
  acciones,
}: Props<T>) {
  const totalColumnas = columnas.length + (acciones ? 1 : 0)

  return (
    <Paper sx={{ borderRadius: '18px', border: '1px solid', borderColor: 'divider', overflow: 'hidden' }}>
      <TableContainer sx={{ maxHeight: '72vh' }}>
        <Table size="small" stickyHeader>
          <TableHead>
            <TableRow
              sx={{
                '& th': {
                  backgroundColor: (theme) => alpha(theme.palette.text.secondary, 0.07),
                  color: 'text.secondary',
                  fontSize: 12,
                  textTransform: 'uppercase',
                  letterSpacing: '0.06em',
                  fontWeight: 700,
                  borderBottom: '1px solid',
                  borderColor: 'divider',
                  py: 1.5,
                },
              }}
            >
              {columnas.map((columna) => (
                <TableCell key={columna.encabezado} align={columna.alineacion ?? 'left'}>
                  {columna.encabezado}
                </TableCell>
              ))}
              {acciones && (
                <TableCell align="right" sx={{ width: 160 }}>
                  Acciones
                </TableCell>
              )}
            </TableRow>
          </TableHead>
          <TableBody>
            {cargando && <EsqueletoTabla filas={Math.min(tamanoPagina, 8)} columnas={totalColumnas} />}
            {!cargando && filas.length === 0 && (
              <TableRow>
                <TableCell colSpan={totalColumnas} sx={{ border: 0 }}>
                  <EstadoVacio mensaje={mensajeVacio} detalle={detalleVacio} />
                </TableCell>
              </TableRow>
            )}
            {!cargando &&
              filas.map((fila) => (
                <TableRow
                  key={claveFila(fila)}
                  hover
                  sx={{
                    '& td': { borderColor: 'divider', py: 1.5 },
                    '&:last-child td': { borderBottom: 0 },
                  }}
                >
                  {columnas.map((columna) => (
                    <TableCell key={columna.encabezado} align={columna.alineacion ?? 'left'}>
                      {columna.render(fila)}
                    </TableCell>
                  ))}
                  {acciones && (
                    <TableCell align="right">
                      <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 0.5 }}>{acciones(fila)}</Box>
                    </TableCell>
                  )}
                </TableRow>
              ))}
          </TableBody>
        </Table>
      </TableContainer>
      <TablePagination
        component="div"
        count={total}
        page={pagina}
        onPageChange={(_, nueva) => onCambiarPagina(nueva)}
        rowsPerPage={tamanoPagina}
        onRowsPerPageChange={(e) => {
          onCambiarTamano(parseInt(e.target.value, 10))
          onCambiarPagina(0)
        }}
        rowsPerPageOptions={[10, 20, 50]}
        labelRowsPerPage="Filas por pagina"
        sx={{ borderTop: '1px solid', borderColor: 'divider' }}
      />
    </Paper>
  )
}