import { zodResolver } from '@hookform/resolvers/zod'
import GoogleIcon from '@mui/icons-material/Google'
import { Alert, Box, Button, Divider, IconButton, InputAdornment, Link, Stack, TextField, Typography } from '@mui/material'
import { Award, BarChart3, ClipboardCheck, Eye, EyeOff, GraduationCap, Lock, Mail } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Navigate, useNavigate, useSearchParams } from 'react-router-dom'
import { z } from 'zod'
import axios from 'axios'
import { API_BASE_URL } from '../api/client'
import { useAuth } from '../auth/useAuth'
import type { ApiError } from '../auth/types'

const loginSchema = z.object({
  email: z.string().min(1, 'El correo es obligatorio').email('Ingresa un correo valido'),
  password: z.string().min(1, 'La contrasena es obligatoria'),
})

type LoginValues = z.infer<typeof loginSchema>

const OAUTH2_HABILITADO = import.meta.env.VITE_OAUTH2_ENABLED !== 'false'

const BENEFICIOS = [
  {
    icono: <ClipboardCheck size={22} />,
    titulo: 'Planeacion didactica',
    texto: 'Registro, revision y aprobacion de la planeacion de cada docente.',
  },
  {
    icono: <BarChart3 size={22} />,
    titulo: 'Avances por parcial',
    texto: 'Estado de cumplimiento con reportes institucionales exportables.',
  },
  {
    icono: <Award size={22} />,
    titulo: 'Evidencias de capacitacion',
    texto: 'Constancias centralizadas y validadas por coordinacion academica.',
  },
]

function mensajeDeError(error: unknown): string {
  if (!axios.isAxiosError(error)) {
    return 'Ocurrio un error inesperado, intenta de nuevo'
  }
  if (!error.response) {
    return 'No se pudo conectar con el servidor, verifica que el backend este activo'
  }

  const estado = error.response.status
  const cuerpo = error.response.data as Partial<ApiError> | undefined

  if (estado === 401) return 'Correo o contrasena incorrectos'
  if (estado === 400) return cuerpo?.message ?? 'Datos invalidos en la solicitud'
  if (estado >= 500) return 'Error del servidor, intenta mas tarde'
  return cuerpo?.message ?? 'No se pudo iniciar sesion'
}

export function LoginPage() {
  const { usuario, cargando, login } = useAuth()
  const navigate = useNavigate()
  const [parametros] = useSearchParams()
  const [verPassword, setVerPassword] = useState(false)

  const oauth2Error = parametros.get('error') === 'oauth2'

  const {
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: '', password: '' },
  })

  if (!cargando && usuario) {
    return <Navigate to="/" replace />
  }

  const onSubmit = handleSubmit(async (values) => {
    try {
      await login(values.email, values.password)
      navigate('/', { replace: true })
    } catch (error) {
      setError('root', { message: mensajeDeError(error) })
    }
  })

  const continuarConGoogle = () => {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/google`
  }

  return (
    <Box
      sx={{
        minHeight: '100vh',
        display: 'grid',
        gridTemplateColumns: { xs: '1fr', md: 'minmax(360px, 44%) 1fr' },
      }}
    >
      <PanelMarca />
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          px: { xs: 3, sm: 6 },
          py: 5,
        }}
      >
        <Stack sx={{ width: '100%', maxWidth: 440 }} spacing={3}>
          <Stack direction="row" sx={{ alignItems: 'center', gap: 1.5 }}>
            <Box
              sx={{
                width: 46,
                height: 46,
                borderRadius: '14px',
                backgroundImage: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
                color: '#fff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                boxShadow: '0 10px 20px -10px rgba(99, 102, 241, 0.9)',
              }}
            >
              <GraduationCap size={26} />
            </Box>
            <Box>
              <Typography variant="subtitle2" color="text.secondary">
                Proyecto Integrador Global
              </Typography>
              <Typography variant="body2" sx={{ fontWeight: 600 }}>
                Seguimiento de Planeacion Didactica
              </Typography>
            </Box>
          </Stack>

          <Box>
            <Typography variant="h5">Bienvenido de vuelta</Typography>
            <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
              Inicia sesion con tu correo institucional para continuar
            </Typography>
          </Box>

          {oauth2Error && (
            <Alert severity="error" variant="filled">
              No se pudo iniciar sesion con Google. Intenta de nuevo.
            </Alert>
          )}

          {errors.root && (
            <Alert severity="error" variant="filled">
              {errors.root.message}
            </Alert>
          )}

          <Box component="form" onSubmit={onSubmit} noValidate>
            <Stack spacing={2.25}>
              <TextField
                label="Correo electronico"
                type="email"
                autoComplete="email"
                fullWidth
                error={!!errors.email}
                helperText={errors.email?.message}
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <Mail size={19} color={errors.email ? '#d32f2f' : '#94a3b8'} />
                      </InputAdornment>
                    ),
                  },
                }}
                {...control.register('email')}
              />

              <TextField
                label="Contrasena"
                type={verPassword ? 'text' : 'password'}
                autoComplete="current-password"
                fullWidth
                error={!!errors.password}
                helperText={errors.password?.message}
                slotProps={{
                  input: {
                    startAdornment: (
                      <InputAdornment position="start">
                        <Lock size={19} color={errors.password ? '#d32f2f' : '#94a3b8'} />
                      </InputAdornment>
                    ),
                    endAdornment: (
                      <InputAdornment position="end">
                        <IconButton
                          aria-label={verPassword ? 'Ocultar contrasena' : 'Mostrar contrasena'}
                          onClick={() => setVerPassword((actual) => !actual)}
                          edge="end"
                          size="small"
                        >
                          {verPassword ? <EyeOff size={19} /> : <Eye size={19} />}
                        </IconButton>
                      </InputAdornment>
                    ),
                  },
                }}
                {...control.register('password')}
              />

              <Button
                type="submit"
                variant="contained"
                size="large"
                fullWidth
                disabled={isSubmitting}
                sx={{ py: 1.25, fontSize: '1rem' }}
              >
                {isSubmitting ? 'Ingresando...' : 'Iniciar sesion'}
              </Button>
            </Stack>
          </Box>

          {OAUTH2_HABILITADO && (
            <>
              <Divider>o continua con</Divider>
              <Button
                variant="outlined"
                size="large"
                fullWidth
                startIcon={<GoogleIcon />}
                onClick={continuarConGoogle}
                disabled={isSubmitting}
                sx={{ py: 1.15, backgroundColor: '#fff' }}
              >
                Google
              </Button>
            </>
          )}

          <Typography variant="body2" color="text.secondary" align="center">
            ¿No tienes cuenta?{' '}
            <Link href="#" underline="hover" sx={{ fontWeight: 600 }}>
              Contacta a coordinacion
            </Link>
          </Typography>
        </Stack>
      </Box>
    </Box>
  )
}

function PanelMarca() {
  return (
    <Box
      sx={{
        display: { xs: 'none', md: 'flex' },
        flexDirection: 'column',
        justifyContent: 'space-between',
        p: { md: 6, lg: 7 },
        color: '#fff',
        backgroundImage: 'linear-gradient(155deg, #4f46e5 0%, #6d28d9 52%, #7c3aed 100%)',
        position: 'relative',
        overflow: 'hidden',
      }}
    >
      <Box
        sx={{
          position: 'absolute',
          width: 360,
          height: 360,
          borderRadius: '50%',
          background: 'rgba(255, 255, 255, 0.08)',
          top: -110,
          right: -130,
        }}
      />
      <Box
        sx={{
          position: 'absolute',
          width: 260,
          height: 260,
          borderRadius: '50%',
          background: 'rgba(255, 255, 255, 0.06)',
          bottom: -80,
          left: -70,
        }}
      />

      <Stack direction="row" sx={{ alignItems: 'center', gap: 1.5, position: 'relative' }}>
        <SchoolMark />
        <Typography sx={{ fontWeight: 700, fontSize: '1.05rem' }}>
          Planeacion Didactica
        </Typography>
      </Stack>

      <Stack spacing={4} sx={{ position: 'relative' }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 800, lineHeight: 1.15 }}>
            Controla el avance
            <br />
            de tu institucion
          </Typography>
          <Typography variant="body1" sx={{ mt: 1.5, opacity: 0.85, maxWidth: 420 }}>
            Centraliza la planeacion, los avances por parcial y las evidencias de
            capacitacion en una sola plataforma.
          </Typography>
        </Box>

        <Stack spacing={2.5}>
          {BENEFICIOS.map((beneficio) => (
            <Stack key={beneficio.titulo} direction="row" sx={{ gap: 2 }}>
              <Box
                sx={{
                  width: 46,
                  height: 46,
                  borderRadius: '13px',
                  background: 'rgba(255, 255, 255, 0.16)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                }}
              >
                {beneficio.icono}
              </Box>
              <Box>
                <Typography sx={{ fontWeight: 600 }}>{beneficio.titulo}</Typography>
                <Typography variant="body2" sx={{ opacity: 0.8, mt: 0.25 }}>
                  {beneficio.texto}
                </Typography>
              </Box>
            </Stack>
          ))}
        </Stack>
      </Stack>

      <Typography variant="body2" sx={{ opacity: 0.7, position: 'relative' }}>
        Desarrollo Web Integral · UTNG · Ciclo 2026
      </Typography>
    </Box>
  )
}

function SchoolMark() {
  return (
    <Box
      sx={{
        width: 42,
        height: 42,
        borderRadius: '12px',
        background: 'rgba(255, 255, 255, 0.18)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
      }}
    >
      <GraduationCap size={24} />
    </Box>
  )
}
