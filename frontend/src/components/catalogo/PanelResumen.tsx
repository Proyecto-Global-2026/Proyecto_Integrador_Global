import Box from '@mui/material/Box'
import { Archive, CheckCircle2, Layers } from 'lucide-react'
import { GraficaDistribucion } from './GraficaDistribucion'
import { TarjetaEstadistica } from './TarjetaEstadistica'

interface Props {
  activos: number
  inactivos: number
}

export function PanelResumen({ activos, inactivos }: Props) {
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: { xs: 'column', md: 'row' },
        gap: 2,
        mb: 3,
      }}
    >
      <Box
        sx={{
          flex: 1,
          display: 'grid',
          gap: 2,
          gridTemplateColumns: { xs: '1fr', sm: 'repeat(3, 1fr)' },
        }}
      >
        <TarjetaEstadistica
          etiqueta="Total registros"
          valor={activos + inactivos}
          icono={<Layers size={22} />}
          acento="#4f46e5"
          fondo="#eef2ff"
        />
        <TarjetaEstadistica
          etiqueta="Activos"
          valor={activos}
          icono={<CheckCircle2 size={22} />}
          acento="#047857"
          fondo="#ecfdf5"
        />
        <TarjetaEstadistica
          etiqueta="Inactivos"
          valor={inactivos}
          icono={<Archive size={22} />}
          acento="#475569"
          fondo="#f1f5f9"
        />
      </Box>
      <GraficaDistribucion activos={activos} inactivos={inactivos} />
    </Box>
  )
}
