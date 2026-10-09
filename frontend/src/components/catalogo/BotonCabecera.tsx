import Button from '@mui/material/Button'
import type { ReactNode } from 'react'

interface Props {
  children: ReactNode
  startIcon?: ReactNode
  onClick?: () => void
  disabled?: boolean
}

export function BotonCabecera({ children, startIcon, onClick, disabled }: Props) {
  return (
    <Button variant="contained" startIcon={startIcon} onClick={onClick} disabled={disabled} sx={{ alignSelf: { xs: 'stretch', sm: 'center' } }}>
      {children}
    </Button>
  )
}