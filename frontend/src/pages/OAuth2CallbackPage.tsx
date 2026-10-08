import { CircularProgress, Stack, Typography } from '@mui/material'
import { useEffect, useRef } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

/**
 * Destino del redirect del backend tras un login OAuth2 exitoso: recibe el
 * JWT en la query, lo valida contra /api/auth/me y entra a la aplicacion.
 */
export function OAuth2CallbackPage() {
  const [parametros] = useSearchParams()
  const navigate = useNavigate()
  const { loginConToken } = useAuth()
  const procesado = useRef(false)

  useEffect(() => {
    if (procesado.current) return
    procesado.current = true

    const token = parametros.get('token')
    if (!token) {
      navigate('/login?error=oauth2', { replace: true })
      return
    }

    loginConToken(token)
      .then(() => navigate('/', { replace: true }))
      .catch(() => navigate('/login?error=oauth2', { replace: true }))
  }, [parametros, loginConToken, navigate])

  return (
    <Stack sx={{ alignItems: 'center', justifyContent: 'center', minHeight: '100vh' }} spacing={2}>
      <CircularProgress />
      <Typography color="text.secondary">Completando inicio de sesion...</Typography>
    </Stack>
  )
}
