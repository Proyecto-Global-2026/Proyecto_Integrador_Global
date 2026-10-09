import Box from '@mui/material/Box'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { alpha } from '@mui/material/styles'
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
        border: '1px solid',
        borderColor: 'divider',
        backgroundImage: (theme) =>
          `linear-gradient(135deg, ${alpha(theme.palette.primary.main, 0.16)} 0%, ${alpha('#A78BFA', 0.1)} 55%, ${alpha(theme.palette.primary.main, 0.05)} 100%)`,
        backdropFilter: 'blur(10px)',
        boxShadow: (theme) => `0 22px 44px -30px ${alpha(theme.palette.primary.main, 0.8)}`,
      }}
    >
      <Box
        sx={{
          position: 'absolute',
          width: 260,
          height: 260,
          right: -90,
          top: -140,
          background: (theme) =>
            `radial-gradient(circle at center, ${alpha(theme.palette.primary.main, 0.28)} 0%, transparent 70%)`,
        }}
      />
      <Box
        sx={{
          position: 'absolute',
          width: 200,
          height: 200,
          right: 130,
          bottom: -150,
          background: 'radial-gradient(circle at center, rgba(167, 139, 250, 0.22) 0%, transparent 70%)',
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
              color: 'primary.main',
              backgroundColor: (theme) => alpha(theme.palette.primary.main, 0.12),
              border: (theme) => `1px solid ${alpha(theme.palette.primary.main, 0.3)}`,
              backdropFilter: 'blur(4px)',
            }}
          >
            {icono}
          </Box>
          <Box>
            <Typography variant="h5">{titulo}</Typography>
            <Typography variant="body2" color="text.secondary">
              {descripcion}
            </Typography>
          </Box>
        </Stack>
        {accion}
      </Stack>
    </Box>
  )
}