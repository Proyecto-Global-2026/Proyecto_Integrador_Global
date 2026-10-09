import Chip from '@mui/material/Chip'
import { CheckCircle2, CircleSlash } from 'lucide-react'

interface Props {
  activo: boolean
  etiquetaActivo?: string
  etiquetaInactivo?: string
}

export function ChipEstado({ activo, etiquetaActivo = 'Activo', etiquetaInactivo = 'Inactivo' }: Props) {
  return (
    <Chip
      size="small"
      variant={activo ? 'filled' : 'outlined'}
      color={activo ? 'success' : 'default'}
      icon={activo ? <CheckCircle2 size={14} /> : <CircleSlash size={14} />}
      label={activo ? etiquetaActivo : etiquetaInactivo}
    />
  )
}