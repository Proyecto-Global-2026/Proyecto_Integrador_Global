import Chip from '@mui/material/Chip'
import { AlertTriangle, CheckCircle2, XCircle } from 'lucide-react'
import { ETIQUETAS_AVANCE, type EstadoAvance } from '../../types/avances'

const CONFIGURACION: Record<
  EstadoAvance,
  { color: 'success' | 'warning' | 'error'; icono: typeof CheckCircle2 }
> = {
  CUMPLIDO: { color: 'success', icono: CheckCircle2 },
  PARCIAL: { color: 'warning', icono: AlertTriangle },
  NO_CUMPLIDO: { color: 'error', icono: XCircle },
}

interface Props {
  estado: EstadoAvance
}

export function ChipEstadoAvance({ estado }: Props) {
  const config = CONFIGURACION[estado]
  const Icono = config.icono
  return (
    <Chip
      size="small"
      color={config.color}
      variant="filled"
      icon={<Icono size={14} />}
      label={ETIQUETAS_AVANCE[estado]}
    />
  )
}