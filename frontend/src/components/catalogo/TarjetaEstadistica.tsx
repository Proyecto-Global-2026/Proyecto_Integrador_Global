import Box from '@mui/material/Box'
import Typography from '@mui/material/Typography'
import { alpha, useTheme } from '@mui/material/styles'
import { animate, useReducedMotion } from 'motion/react'
import { useEffect, useState, type ReactNode } from 'react'

type Variedad = 'primario' | 'exito' | 'neutro'

interface Props {
  etiqueta: string
  valor: number
  icono: ReactNode
  variedad?: Variedad
}

function useContador(valor: number) {
  const [actual, setActual] = useState(0)
  const reducir = useReducedMotion()
  useEffect(() => {
    const animacion = animate(0, valor, {
      duration: reducir ? 0 : 0.6,
      ease: 'easeOut',
      onUpdate: (v) => setActual(Math.round(v)),
    })
    return () => animacion.stop()
  }, [valor, reducir])
  return actual
}

export function TarjetaEstadistica({ etiqueta, valor, icono, variedad = 'primario' }: Props) {
  const tema = useTheme()
  const color =
    variedad === 'exito'
      ? tema.palette.success.main
      : variedad === 'primario'
        ? tema.palette.primary.main
        : tema.palette.text.secondary
  const cifra = useContador(valor)

  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: 1.75,
        p: 2,
        borderRadius: '18px',
        border: '1px solid',
        borderColor: 'divider',
        bgcolor: 'background.paper',
        transition: 'transform 0.2s ease, box-shadow 0.2s ease',
        '&:hover': {
          transform: 'translateY(-2px)',
          boxShadow: `0 18px 30px -24px ${alpha(color, 0.55)}`,
        },
      }}
    >
      <Box
        sx={{
          width: 46,
          height: 46,
          flexShrink: 0,
          borderRadius: '14px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          color,
          backgroundColor: alpha(color, 0.12),
          boxShadow: `inset 0 0 0 1px ${alpha(color, 0.25)}`,
        }}
      >
        {icono}
      </Box>
      <Box>
        <Typography variant="h5" sx={{ fontVariantNumeric: 'tabular-nums', lineHeight: 1.1 }}>
          {cifra.toLocaleString('es-MX')}
        </Typography>
        <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
          {etiqueta}
        </Typography>
      </Box>
    </Box>
  )
}