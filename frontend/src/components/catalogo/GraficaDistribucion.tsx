import Box from '@mui/material/Box'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { Cell, Pie, PieChart, ResponsiveContainer } from 'recharts'

interface Props {
  activos: number
  inactivos: number
}

export function GraficaDistribucion({ activos, inactivos }: Props) {
  const total = activos + inactivos
  const datos = [
    { nombre: 'Activos', valor: activos, color: '#10b981' },
    { nombre: 'Inactivos', valor: inactivos, color: '#cbd5e1' },
  ]

  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: 2,
        p: 2,
        borderRadius: 3,
        border: '1px solid #eef2f7',
        background: '#fff',
        minWidth: 230,
      }}
    >
      <Box sx={{ position: 'relative', width: 140, height: 140, flexShrink: 0 }}>
        <ResponsiveContainer width="100%" height="100%">
          <PieChart>
            <Pie
              data={total === 0 ? [{ nombre: 'Sin datos', valor: 1, color: '#e2e8f0' }] : datos}
              dataKey="valor"
              nameKey="nombre"
              innerRadius={44}
              outerRadius={64}
              paddingAngle={total === 0 ? 0 : 3}
              stroke="none"
            >
              {(total === 0 ? [{ color: '#e2e8f0' }] : datos).map((entrada) => (
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
          <Typography variant="h6" sx={{ lineHeight: 1 }}>
            {total}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            Total
          </Typography>
        </Box>
      </Box>
      <Stack spacing={1.25}>
        {datos.map((d) => (
          <Stack key={d.nombre} direction="row" spacing={1} sx={{ alignItems: 'center' }}>
            <Box sx={{ width: 10, height: 10, borderRadius: '50%', background: d.color }} />
            <Typography variant="body2" sx={{ fontWeight: 600 }}>
              {d.nombre}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {d.valor}
            </Typography>
          </Stack>
        ))}
      </Stack>
    </Box>
  )
}
