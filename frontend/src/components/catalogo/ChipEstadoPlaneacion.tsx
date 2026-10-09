import Chip from '@mui/material/Chip'
import { AlertTriangle, CheckCircle2, Clock, XCircle } from 'lucide-react'
import type { EstadoPlaneacion } from '../../types/planeaciones'

const CONFIGURACION: Record<
  EstadoPlaneacion,
  { etiqueta: string; color: 'info' | 'warning' | 'success' | 'error'; icono: typeof Clock }
> = {
  PENDIENTE: { etiqueta: 'Pendiente', color: 'info', icono: Clock },
  AJUSTES_SOLICITADOS: { etiqueta: 'Ajustes solicitados', color: 'warning', icono: AlertTriangle },
  APROBADA: { etiqueta: 'Aprobada', color: 'success', icono: CheckCircle2 },
  RECHAZADA: { etiqueta: 'Rechazada', color: 'error', icono: XCircle },
}

interface Props {
  estado: EstadoPlaneacion
}

export function ChipEstadoPlaneacion({ estado }: Props) {
  const config = CONFIGURACION[estado]
  const Icono = config.icono
  return (
    <Chip size="small" color={config.color} variant="filled" icon={<Icono size={14} />} label={config.etiqueta} />
  )
}