import Box from '@mui/material/Box'
import CircularProgress from '@mui/material/CircularProgress'
import Paper from '@mui/material/Paper'
import Stack from '@mui/material/Stack'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TablePagination from '@mui/material/TablePagination'
import TableRow from '@mui/material/TableRow'
import Typography from '@mui/material/Typography'
import { Inbox } from 'lucide-react'
import type { ReactNode } from 'react'

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
  acciones,
}: Props<T>) {
  const totalColumnas = columnas.length + (acciones ? 1 : 0)

  return (
    <Paper sx={{ borderRadius: 3, border: '1px solid #eef2f7', overflow: 'hidden' }}>
      <TableContainer>
        <Table size="small">
          <TableHead>
            <TableRow
              sx={{
                '& th': {
                  background: '#f8fafc',
                  color: '#64748b',
                  fontSize: 12,
                  textTransform: 'uppercase',
                  letterSpacing: '0.06em',
                  fontWeight: 700,
                  borderBottom: '1px solid #eef2f7',
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
            {cargando && (
              <TableRow>
                <TableCell colSpan={totalColumnas} align="center" sx={{ border: 0 }}>
                  <Stack direction="row" spacing={1.5} sx={{ justifyContent: 'center', alignItems: 'center', py: 5 }}>
                    <CircularProgress size={22} />
                    <Typography variant="body2" color="text.secondary">
                      Cargando...
                    </Typography>
                  </Stack>
                </TableCell>
              </TableRow>
            )}
            {!cargando && filas.length === 0 && (
              <TableRow>
                <TableCell colSpan={totalColumnas} align="center" sx={{ border: 0 }}>
                  <Stack spacing={1.25} sx={{ alignItems: 'center', py: 6 }}>
                    <Box
                      sx={{
                        width: 56,
                        height: 56,
                        borderRadius: '16px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: '#94a3b8',
                        background: '#f1f5f9',
                      }}
                    >
                      <Inbox size={26} />
                    </Box>
                    <Typography variant="body2" color="text.secondary">
                      {mensajeVacio}
                    </Typography>
                  </Stack>
                </TableCell>
              </TableRow>
            )}
            {!cargando &&
              filas.map((fila) => (
                <TableRow
                  key={claveFila(fila)}
                  hover
                  sx={{
                    '& td': { borderColor: '#f1f5f9', py: 1.5 },
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
        sx={{ borderTop: '1px solid #eef2f7' }}
      />
    </Paper>
  )
}
