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
        <TarjetaEstadistica etiqueta="Total registros" valor={activos + inactivos} icono={<Layers size={22} />} variedad="primario" />
        <TarjetaEstadistica etiqueta="Activos" valor={activos} icono={<CheckCircle2 size={22} />} variedad="exito" />
        <TarjetaEstadistica etiqueta="Inactivos" valor={inactivos} icono={<Archive size={22} />} variedad="neutro" />
      </Box>
      <GraficaDistribucion activos={activos} inactivos={inactivos} />
    </Box>
  )
}