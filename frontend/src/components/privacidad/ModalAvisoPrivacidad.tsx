import { useState } from 'react'
import Button from '@mui/material/Button'
import Checkbox from '@mui/material/Checkbox'
import Dialog from '@mui/material/Dialog'
import DialogActions from '@mui/material/DialogActions'
import DialogContent from '@mui/material/DialogContent'
import DialogTitle from '@mui/material/DialogTitle'
import FormControlLabel from '@mui/material/FormControlLabel'
import { AvisoPrivacidadSimplificado } from './AvisoPrivacidadSimplificado'
import { EnlaceAvisoPrivacidad } from './EnlaceAvisoPrivacidad'

const CLAVE_ACEPTACION = 'aviso_privacidad_registro_aceptado'

interface Props {
  open: boolean
  onAceptar: () => void
}

export function ModalAvisoPrivacidad({ open, onAceptar }: Props) {
  const [aceptado, setAceptado] = useState(false)

  const handleAceptar = () => {
    sessionStorage.setItem(CLAVE_ACEPTACION, 'true')
    // Se reinicia para que una nueva apertura exija aceptar de nuevo.
    setAceptado(false)
    onAceptar()
  }

  return (
    <Dialog open={open} maxWidth="md" fullWidth>
      <DialogTitle>Aviso de Privacidad</DialogTitle>
      <DialogContent dividers>
        <AvisoPrivacidadSimplificado />
        <FormControlLabel
          sx={{ mt: 2 }}
          control={<Checkbox checked={aceptado} onChange={(e) => setAceptado(e.target.checked)} />}
          label="He leído y acepto el Aviso de Privacidad Simplificado."
        />
        <EnlaceAvisoPrivacidad />
      </DialogContent>
      <DialogActions>
        <Button variant="contained" disabled={!aceptado} onClick={handleAceptar}>
          Aceptar y continuar
        </Button>
      </DialogActions>
    </Dialog>
  )
}
