import Box from '@mui/material/Box'
import Typography from '@mui/material/Typography'
import { alpha } from '@mui/material/styles'
import { Inbox } from 'lucide-react'
import type { ReactNode } from 'react'

interface Props {
  mensaje: string
  detalle?: string
  icono?: ReactNode
  accion?: ReactNode
}

export function EstadoVacio({ mensaje, detalle, icono, accion }: Props) {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 1.5, py: 6, px: 2 }}>
      <Box
        sx={{
          width: 58,
          height: 58,
          borderRadius: '18px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: (theme) => alpha(theme.palette.primary.main, 0.85),
          backgroundColor: (theme) => alpha(theme.palette.primary.main, 0.1),
          border: (theme) => `1px solid ${alpha(theme.palette.primary.main, 0.25)}`,
        }}
      >
        {icono ?? <Inbox size={26} />}
      </Box>
      <Typography variant="body1" sx={{ fontWeight: 600 }}>
        {mensaje}
      </Typography>
      {detalle && (
        <Typography variant="body2" color="text.secondary" sx={{ textAlign: 'center', maxWidth: 360 }}>
          {detalle}
        </Typography>
      )}
      {accion}
    </Box>
  )
}