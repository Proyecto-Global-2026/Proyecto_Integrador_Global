import Box from '@mui/material/Box'
import Typography from '@mui/material/Typography'
import type { ReactNode } from 'react'

interface Props {
  etiqueta: string
  valor: number
  icono: ReactNode
  acento: string
  fondo: string
}

export function TarjetaEstadistica({ etiqueta, valor, icono, acento, fondo }: Props) {
  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: 1.75,
        p: 2,
        borderRadius: 3,
        border: '1px solid #eef2f7',
        background: '#fff',
        transition: 'transform 0.2s ease, box-shadow 0.2s ease',
        '&:hover': { transform: 'translateY(-2px)', boxShadow: '0 18px 30px -24px rgba(15, 23, 42, 0.45)' },
      }}
    >
      <Box
        sx={{
          width: 46,
          height: 46,
          borderRadius: '14px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color: acento,
          background: fondo,
        }}
      >
        {icono}
      </Box>
      <Box>
        <Typography variant="h5" sx={{ lineHeight: 1.1 }}>
          {valor}
        </Typography>
        <Typography variant="caption" sx={{ color: 'text.secondary', fontWeight: 600 }}>
          {etiqueta}
        </Typography>
      </Box>
    </Box>
  )
}
