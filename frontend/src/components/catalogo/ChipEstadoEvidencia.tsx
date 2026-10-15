import Chip from '@mui/material/Chip'
import { CheckCircle2, Clock, XCircle } from 'lucide-react'
import type { EstadoEvidencia } from '../../types/evidencias'

const CONFIGURACION: Record<
  EstadoEvidencia,
  { etiqueta: string; color: 'info' | 'success' | 'error'; icono: typeof Clock }
> = {
  PENDIENTE: { etiqueta: 'Pendiente', color: 'info', icono: Clock },
  APROBADA: { etiqueta: 'Aprobada', color: 'success', icono: CheckCircle2 },
  RECHAZADA: { etiqueta: 'Rechazada', color: 'error', icono: XCircle },
}

interface Props {
  estado: EstadoEvidencia
}

export function ChipEstadoEvidencia({ estado }: Props) {
  const config = CONFIGURACION[estado]
  const Icono = config.icono
  return (
    <Chip size="small" color={config.color} variant="filled" icon={<Icono size={14} />} label={config.etiqueta} />
  )
}
