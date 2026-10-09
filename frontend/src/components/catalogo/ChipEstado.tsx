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
      variant="outlined"
      icon={activo ? <CheckCircle2 size={14} /> : <CircleSlash size={14} />}
      label={activo ? etiquetaActivo : etiquetaInactivo}
      sx={
        activo
          ? { color: '#047857', borderColor: '#a7f3d0', background: '#ecfdf5' }
          : { color: '#64748b', borderColor: '#e2e8f0', background: '#f8fafc' }
      }
    />
  )
}
