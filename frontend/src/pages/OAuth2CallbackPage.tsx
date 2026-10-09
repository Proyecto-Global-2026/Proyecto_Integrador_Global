import Box from '@mui/material/Box'
import CircularProgress from '@mui/material/CircularProgress'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { motion, useReducedMotion } from 'motion/react'
import { GraduationCap } from 'lucide-react'
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
  const reducir = useReducedMotion()

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
    <Box
      sx={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        px: 2,
        bgcolor: 'background.default',
      }}
    >
      <Stack spacing={3} sx={{ alignItems: 'center' }}>
        <motion.div
          animate={reducir ? undefined : { scale: [1, 1.08, 1], boxShadow: ['0 0 0px rgba(34,211,238,0)', '0 0 34px -6px rgba(34,211,238,0.7)', '0 0 0px rgba(34,211,238,0)'] }}
          transition={{ duration: 1.6, repeat: Infinity, ease: 'easeInOut' }}
        >
          <Box
            sx={{
              width: 66,
              height: 66,
              borderRadius: '20px',
              backgroundImage: 'linear-gradient(135deg, #22D3EE 0%, #A78BFA 100%)',
              color: '#041318',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <GraduationCap size={32} />
          </Box>
        </motion.div>

        <CircularProgress size={30} />

        <Box sx={{ textAlign: 'center' }}>
          <Typography variant="h6">Completando inicio de sesión</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            Validando tu cuenta institucional...
          </Typography>
        </Box>
      </Stack>
    </Box>
  )
}