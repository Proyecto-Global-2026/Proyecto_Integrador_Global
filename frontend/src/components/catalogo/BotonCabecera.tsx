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
    <Button
      variant="contained"
      startIcon={startIcon}
      onClick={onClick}
      disabled={disabled}
      sx={{
        alignSelf: { xs: 'stretch', sm: 'center' },
        background: '#ffffff',
        backgroundImage: 'none',
        color: '#4338ca',
        boxShadow: '0 12px 24px -14px rgba(15, 23, 42, 0.6)',
        '&:hover': {
          background: '#eef2ff',
          backgroundImage: 'none',
          transform: 'translateY(-1px)',
          boxShadow: '0 16px 28px -14px rgba(15, 23, 42, 0.65)',
        },
        '&.Mui-disabled': { background: 'rgba(255,255,255,0.65)', color: '#a5b4fc' },
      }}
    >
      {children}
    </Button>
  )
}
