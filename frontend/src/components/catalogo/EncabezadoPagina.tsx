import Box from '@mui/material/Box'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import type { ReactNode } from 'react'

interface Props {
  titulo: string
  descripcion: string
  icono: ReactNode
  accion?: ReactNode
}

export function EncabezadoPagina({ titulo, descripcion, icono, accion }: Props) {
  return (
    <Box
      sx={{
        position: 'relative',
        overflow: 'hidden',
        borderRadius: 4,
        p: { xs: 2.5, sm: 3 },
        mb: 3,
        color: '#fff',
        backgroundImage: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 55%, #a855f7 100%)',
        boxShadow: '0 26px 44px -26px rgba(99, 102, 241, 0.95)',
      }}
    >
      <Box
        sx={{
          position: 'absolute',
          width: 240,
          height: 240,
          borderRadius: '50%',
          right: -70,
          top: -110,
          background: 'rgba(255, 255, 255, 0.12)',
        }}
      />
      <Box
        sx={{
          position: 'absolute',
          width: 170,
          height: 170,
          borderRadius: '50%',
          right: 80,
          bottom: -120,
          background: 'rgba(255, 255, 255, 0.08)',
        }}
      />
      <Stack
        direction={{ xs: 'column', sm: 'row' }}
        spacing={2}
        sx={{ position: 'relative', alignItems: { xs: 'flex-start', sm: 'center' }, justifyContent: 'space-between' }}
      >
        <Stack direction="row" spacing={2} sx={{ alignItems: 'center' }}>
          <Box
            sx={{
              width: 54,
              height: 54,
              flexShrink: 0,
              borderRadius: '16px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              background: 'rgba(255, 255, 255, 0.2)',
              border: '1px solid rgba(255, 255, 255, 0.35)',
              backdropFilter: 'blur(4px)',
            }}
          >
            {icono}
          </Box>
          <Box>
            <Typography variant="h5" sx={{ color: '#fff' }}>
              {titulo}
            </Typography>
            <Typography variant="body2" sx={{ color: 'rgba(255, 255, 255, 0.85)' }}>
              {descripcion}
            </Typography>
          </Box>
        </Stack>
        {accion}
      </Stack>
    </Box>
  )
}
