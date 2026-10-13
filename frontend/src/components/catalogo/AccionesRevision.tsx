import axios from 'axios'
import Button from '@mui/material/Button'
import Stack from '@mui/material/Stack'
import TextField from '@mui/material/TextField'
import { CheckCircle2, RefreshCw, XCircle } from 'lucide-react'
import { useState } from 'react'
import { sileo } from 'sileo'
import { revisarPlaneacion } from '../../api/revisiones'
import type { DecisionRevision } from '../../types/revisiones'
import { ConfirmarAccion } from './ConfirmarAccion'

const TEXTO_DECISION: Record<DecisionRevision, string> = {
  APROBADA: 'Aprobar planeacion',
  AJUSTES_SOLICITADOS: 'Solicitar ajustes',
  RECHAZADA: 'Rechazar planeacion',
}

const COLOR_DECISION: Record<DecisionRevision, 'success' | 'warning' | 'error'> = {
  APROBADA: 'success',
  AJUSTES_SOLICITADOS: 'warning',
  RECHAZADA: 'error',
}

const TEXTO_CONFIRMAR: Record<DecisionRevision, string> = {
  APROBADA: 'Aprobar',
  AJUSTES_SOLICITADOS: 'Solicitar ajustes',
  RECHAZADA: 'Rechazar',
}

interface Props {
  planeacionId: string
  titulo: string
  onRevisado: () => void
}

export function AccionesRevision({ planeacionId, titulo, onRevisado }: Props) {
  const [comentario, setComentario] = useState('')
  const [errorComentario, setErrorComentario] = useState<string | null>(null)
  const [decision, setDecision] = useState<DecisionRevision | null>(null)
  const [enviando, setEnviando] = useState(false)

  const solicitar = (estado: DecisionRevision) => {
    const requiereComentario = estado === 'RECHAZADA' || estado === 'AJUSTES_SOLICITADOS'
    if (requiereComentario && !comentario.trim()) {
      setErrorComentario('El comentario es obligatorio al rechazar o solicitar ajustes')
      return
    }
    setErrorComentario(null)
    setDecision(estado)
  }

  const confirmar = async () => {
    if (!decision) return
    const estado = decision
    setEnviando(true)
    try {
      await revisarPlaneacion(planeacionId, {
        estado,
        comentario: comentario.trim() ? comentario.trim() : undefined,
      })
      sileo.success({ title: `${TEXTO_DECISION[estado]} registrada` })
      setComentario('')
      setDecision(null)
      onRevisado()
    } catch (error) {
      setDecision(null)
      sileo.error({ title: mensajeDeError(error) })
    } finally {
      setEnviando(false)
    }
  }

  return (
    <>
      <TextField
        label="Comentario de revision"
        fullWidth
        multiline
        minRows={3}
        value={comentario}
        onChange={(e) => {
          setComentario(e.target.value)
          if (errorComentario) setErrorComentario(null)
        }}
        error={!!errorComentario}
        helperText={errorComentario ?? 'Obligatorio al rechazar o solicitar ajustes. Opcional al aprobar.'}
        slotProps={{ htmlInput: { maxLength: 1000 } }}
      />
      <Stack direction="row" spacing={1} sx={{ justifyContent: 'flex-end', flexWrap: 'wrap' }}>
        <Button
          onClick={() => solicitar('RECHAZADA')}
          color="error"
          variant="outlined"
          startIcon={<XCircle size={16} />}
          disabled={enviando}
        >
          Rechazar
        </Button>
        <Button
          onClick={() => solicitar('AJUSTES_SOLICITADOS')}
          color="warning"
          variant="outlined"
          startIcon={<RefreshCw size={16} />}
          disabled={enviando}
        >
          Solicitar ajustes
        </Button>
        <Button
          onClick={() => solicitar('APROBADA')}
          color="success"
          variant="contained"
          startIcon={<CheckCircle2 size={16} />}
          disabled={enviando}
        >
          Aprobar
        </Button>
      </Stack>

      <ConfirmarAccion
        abierto={decision !== null}
        titulo={decision ? TEXTO_DECISION[decision] : ''}
        mensaje={
          decision === 'APROBADA'
            ? `Se aprobara la planeacion "${titulo}".`
            : `Se registrara la decision en la planeacion "${titulo}".`
        }
        textoConfirmar={decision ? TEXTO_CONFIRMAR[decision] : 'Confirmar'}
        color={decision ? COLOR_DECISION[decision] : 'primary'}
        onConfirmar={() => void confirmar()}
        onCerrar={() => setDecision(null)}
      />
    </>
  )
}

function mensajeDeError(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const cuerpo = error.response?.data as { message?: string } | undefined
    if (cuerpo?.message) return cuerpo.message
  }
  return 'No se pudo registrar la revision'
}