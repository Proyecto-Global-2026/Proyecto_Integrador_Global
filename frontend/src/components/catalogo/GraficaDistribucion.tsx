import Box from '@mui/material/Box'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { alpha, useTheme } from '@mui/material/styles'
import { Cell, Pie, PieChart, ResponsiveContainer } from 'recharts'

interface Props {
  activos: number
  inactivos: number
}

export function GraficaDistribucion({ activos, inactivos }: Props) {
  const tema = useTheme()
  const colorActivo = tema.palette.success.main
  const colorInactivo = alpha(tema.palette.text.secondary, 0.35)
  const total = activos + inactivos
  const datos = [
    { nombre: 'Activos', valor: activos, color: colorActivo },
    { nombre: 'Inactivos', valor: inactivos, color: colorInactivo },
  ]

  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: 2,
        p: 2,
        borderRadius: '18px',
        border: '1px solid',
        borderColor: 'divider',
        bgcolor: 'background.paper',
        minWidth: 230,
      }}
    >
      <Box sx={{ position: 'relative', width: 140, height: 140, flexShrink: 0 }}>
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={total === 0 ? [{ nombre: 'Sin datos', valor: 1, color: colorInactivo }] : datos}
              dataKey="valor"
              nameKey="nombre"
              innerRadius={44}
              outerRadius={64}
              paddingAngle={total === 0 ? 0 : 3}
              stroke="none"
            >
              {(total === 0 ? [{ color: colorInactivo }] : datos).map((entrada) => (
                <Cell key={entrada.color} fill={entrada.color} />
              ))}
            </Pie>
          </PieChart>
        </ResponsiveContainer>
        <Box
          sx={{
            position: 'absolute',
            inset: 0,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            pointerEvents: 'none',
          }}
        >
          <Typography variant="h6" sx={{ lineHeight: 1, fontVariantNumeric: 'tabular-nums' }}>
            {total.toLocaleString('es-MX')}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Total
          </Typography>
        </Box>
      </Box>
      <Stack spacing={1.25}>
        {datos.map((d) => (
          <Stack key={d.nombre} direction="row" spacing={1} sx={{ alignItems: 'center' }}>
            <Box sx={{ width: 10, height: 10, borderRadius: '50%', backgroundColor: d.color }} />
            <Typography variant="body2" sx={{ fontWeight: 600 }}>
              {d.nombre}
            </Typography>
            <Typography variant="body2" color="text.secondary" sx={{ fontVariantNumeric: 'tabular-nums' }}>
              {d.valor.toLocaleString('es-MX')}
            </Typography>
          </Stack>
        ))}
      </Stack>
    </Box>
  )
}