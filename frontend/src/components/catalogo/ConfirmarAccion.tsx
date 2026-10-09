import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import Typography from '@mui/material/Typography'
import { AlertTriangle } from 'lucide-react'
import type { ReactNode } from 'react'

interface Props {
  abierto: boolean
  titulo: string
  mensaje: ReactNode
  textoConfirmar: string
  color?: 'warning' | 'success' | 'error' | 'primary'
  onConfirmar: () => void
  onCerrar: () => void
}

export function ConfirmarAccion({
  abierto,
  titulo,
  mensaje,
  textoConfirmar,
  color = 'warning',
  onConfirmar,
  onCerrar,
}: Props) {
  return (
    <Dialog open={abierto} onClose={onCerrar} maxWidth="xs" fullWidth>
      <DialogTitle sx={{ pb: 0 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25 }}>
          <Box
            sx={{
              width: 40,
              height: 40,
              borderRadius: '12px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#b45309',
              background: '#fef3c7',
            }}
          >
            <AlertTriangle size={20} />
          </Box>
          {titulo}
        </Box>
      </DialogTitle>
      <DialogContent>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          {mensaje}
        </Typography>
      </DialogContent>
      <DialogActions sx={{ px: 3, pb: 2 }}>
        <Button onClick={onCerrar} color="inherit">
          Cancelar
        </Button>
        <Button onClick={onConfirmar} variant="contained" color={color}>
          {textoConfirmar}
        </Button>
      </DialogActions>
    </Dialog>
  )
}
