import Skeleton from '@mui/material/Skeleton'
import TableCell from '@mui/material/TableCell'
import TableRow from '@mui/material/TableRow'

interface Props {
  filas?: number
  columnas?: number
}

export function EsqueletoTabla({ filas = 8, columnas = 4 }: Props) {
  return (
    <>
      {Array.from({ length: filas }).map((_, indice) => (
        <TableRow key={indice} sx={{ '& td': { borderBottom: '1px solid', borderColor: 'divider', py: 1.5 } }}>
          {Array.from({ length: columnas }).map((_, columna) => (
            <TableCell key={`${indice}-${columna}`}>
              <Skeleton variant="text" width={columna === 0 ? '72%' : `${70 - columna * 10}%`} />
            </TableCell>
          ))}
        </TableRow>
      ))}
    </>
  )
}