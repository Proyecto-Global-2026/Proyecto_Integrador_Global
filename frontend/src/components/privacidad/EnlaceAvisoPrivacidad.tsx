import { Link } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'

interface Props {
  texto?: string
}

export function EnlaceAvisoPrivacidad({ texto = 'Aviso de Privacidad Integral' }: Props) {
  return (
    <Link component={RouterLink} to="/aviso-privacidad" target="_blank" rel="noopener noreferrer" underline="hover">
      {texto}
    </Link>
  )
}
