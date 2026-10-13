import Box from '@mui/material/Box'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { alpha } from '@mui/material/styles'
import dayjs from 'dayjs'
import { History } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { listarRevisiones } from '../../api/revisiones'
import type { EstadoPlaneacion } from '../../types/planeaciones'
import type { Revision } from '../../types/revisiones'
import { ChipEstadoPlaneacion } from './ChipEstadoPlaneacion'

interface Props {
  planeacionId: string
}

export function HistorialRevisiones({ planeacionId }: Props) {
  const [revisiones, setRevisiones] = useState<Revision[]>([])

  const cargar = useCallback(async () => {
    setRevisiones(await listarRevisiones(planeacionId))
  }, [planeacionId])

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      try {
        if (!cancelado) await cargar()
      } catch {
        if (!cancelado) setRevisiones([])
      }
    }
    void ejecutar()
    return () => {
      cancelado = true
    }
  }, [cargar])

  if (revisiones.length === 0) return null

  const ordenadas = [...revisiones].sort(
    (a, b) => dayjs(b.createdAt).valueOf() - dayjs(a.createdAt).valueOf(),
  )

  return (
    <Box>
      <Typography
        variant="caption"
        color="text.secondary"
        sx={{
          fontWeight: 700,
          textTransform: 'uppercase',
          letterSpacing: '0.05em',
          display: 'flex',
          alignItems: 'center',
          gap: 0.75,
        }}
      >
        <History size={14} /> Historial de revisiones
      </Typography>
      <Stack spacing={1.25} sx={{ mt: 1.5 }}>
        {ordenadas.map((revision) => (
          <Box
            key={revision.id}
            sx={{
              p: 1.5,
              borderRadius: 2,
              border: '1px solid',
              borderColor: 'divider',
              bgcolor: 'background.paper',
            }}
          >
            <Stack
              direction="row"
              sx={{ alignItems: 'center', justifyContent: 'space-between', gap: 1, flexWrap: 'wrap' }}
            >
              <ChipEstadoPlaneacion estado={revision.estadoNuevo as EstadoPlaneacion} />
              <Typography variant="caption" color="text.secondary">
                {dayjs(revision.createdAt).format('DD/MM/YYYY HH:mm')}
              </Typography>
            </Stack>
            <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 0.75 }}>
              por {revision.revisadoPorNombre}
            </Typography>
            {revision.comentario && (
              <Box
                sx={{
                  mt: 1,
                  p: 1.25,
                  borderRadius: 1.5,
                  bgcolor: (theme) => alpha(theme.palette.text.primary, 0.04),
                  borderLeft: '3px solid',
                  borderColor: 'primary.main',
                }}
              >
                <Typography variant="body2" sx={{ whiteSpace: 'pre-wrap' }}>
                  {revision.comentario}
                </Typography>
              </Box>
            )}
          </Box>
        ))}
      </Stack>
    </Box>
  )
}