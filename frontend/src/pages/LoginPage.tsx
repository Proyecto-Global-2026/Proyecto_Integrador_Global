import { zodResolver } from '@hookform/resolvers/zod'
import GoogleIcon from '@mui/icons-material/Google'
import { Alert, Box, Button, Card, Divider, IconButton, InputAdornment, Link, Stack, TextField, Typography } from '@mui/material'
import { alpha } from '@mui/material/styles'
import { Award, BarChart3, ClipboardCheck, Eye, EyeOff, GraduationCap, Lock, Mail } from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Navigate, useLocation, useNavigate, useSearchParams } from 'react-router-dom'
import { z } from 'zod'
import axios from 'axios'
import heroUrl from '../assets/hero.png'
import { API_BASE_URL } from '../api/client'
import { useAuth } from '../auth/useAuth'
import type { ApiError } from '../auth/types'
import { useTema } from '../theme/contextoTema'

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
  const ubicacion = useLocation()
  const [parametros] = useSearchParams()
  const [verPassword, setVerPassword] = useState(false)

  const oauth2Error = parametros.get('error') === 'oauth2'
  const sesionExpirada = (ubicacion.state as { sesionExpirada?: boolean } | null)?.sesionExpirada === true

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
        gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)' },
        bgcolor: 'background.default',
      }}
    >
      <PanelMarca />
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          px: { xs: 2, sm: 4, md: 6 },
          py: { xs: 3, sm: 5 },
        }}
      >
        <Card
          sx={{
            width: '100%',
            maxWidth: 480,
            borderRadius: '24px',
            p: { xs: 3, sm: 4 },
            boxShadow: (theme) => `0 40px 80px -50px ${alpha(theme.palette.primary.main, 0.6)}`,
          }}
        >
          <Stack spacing={2.5}>
            <Stack direction="row" sx={{ alignItems: 'center', gap: 1.5 }}>
              <Box
                sx={{
                  width: 44,
                  height: 44,
                  borderRadius: '13px',
                  backgroundImage: 'linear-gradient(135deg, #22D3EE 0%, #A78BFA 100%)',
                  color: '#041318',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  boxShadow: '0 10px 20px -10px rgba(34, 211, 238, 0.9)',
                }}
              >
                <GraduationCap size={25} />
              </Box>
              <Box>
                <Typography variant="subtitle2" color="text.secondary" sx={{ lineHeight: 1.2 }}>
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

            {sesionExpirada && (
              <Alert severity="warning" variant="filled">
                Tu sesion expiro o caduco. Inicia sesion de nuevo para continuar.
              </Alert>
            )}

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
                        <InputAdornment position="start" sx={{ color: errors.email ? 'error.main' : 'text.secondary' }}>
                          <Mail size={19} />
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
                        <InputAdornment position="start" sx={{ color: errors.password ? 'error.main' : 'text.secondary' }}>
                          <Lock size={19} />
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
                <Button variant="outlined" size="large" fullWidth startIcon={<GoogleIcon />} onClick={continuarConGoogle} disabled={isSubmitting} sx={{ py: 1.15 }}>
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
        </Card>
      </Box>
    </Box>
  )
}

function PanelMarca() {
  const { modo } = useTema()
  const esOscuro = modo === 'dark'

  return (
    <Box
      sx={{
        position: 'relative',
        overflow: 'hidden',
        display: { xs: 'none', md: 'flex' },
        flexDirection: 'column',
        justifyContent: 'space-between',
        gap: 3.5,
        p: { md: 5, lg: 6 },
        borderRight: '1px solid',
        borderColor: 'divider',
        backgroundImage: (theme) =>
          `linear-gradient(170deg, ${alpha(theme.palette.primary.main, esOscuro ? 0.22 : 0.5)} 0%, ${alpha('#A78BFA', 0.16)} 55%, transparent 100%)`,
      }}
    >
      <Box
        sx={{
          position: 'absolute',
          inset: 0,
          backgroundImage: (theme) =>
            `linear-gradient(${alpha(theme.palette.text.secondary, 0.05)} 1px, transparent 1px), linear-gradient(90deg, ${alpha(theme.palette.text.secondary, 0.05)} 1px, transparent 1px)`,
          backgroundSize: '44px 44px',
          maskImage: 'radial-gradient(ellipse at 30% 20%, black 30%, transparent 75%)',
        }}
      />
      <Box
        sx={{
          position: 'absolute',
          width: 340,
          height: 340,
          borderRadius: '50%',
          top: -120,
          right: -120,
          background: (theme) => `radial-gradient(circle, ${alpha(theme.palette.primary.main, esOscuro ? 0.3 : 0.35)} 0%, transparent 70%)`,
        }}
      />
      <Box
        sx={{
          position: 'absolute',
          width: 300,
          height: 300,
          borderRadius: '50%',
          bottom: -140,
          left: -120,
          background: 'radial-gradient(circle, rgba(167, 139, 250, 0.28) 0%, transparent 70%)',
        }}
      />

      <Stack direction="row" sx={{ alignItems: 'center', gap: 1.5, position: 'relative', flexShrink: 0 }}>
        <SchoolMark />
        <Box>
          <Typography sx={{ fontWeight: 700, lineHeight: 1.1 }}>Planeacion Didactica</Typography>
          <Typography variant="caption" color="text.secondary">
            UTNG · Desarrollo Web Integral
          </Typography>
        </Box>
      </Stack>

      <Stack spacing={3} sx={{ position: 'relative', justifyContent: 'center', minHeight: 0, overflowY: 'auto', py: 0.5 }}>
        <Box
          sx={{
            width: 'min(100%, 340px)',
            height: 190,
            borderRadius: '22px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            px: 3,
            backgroundColor: (theme) => alpha(theme.palette.primary.main, esOscuro ? 0.1 : 0.12),
            border: (theme) => `1px solid ${alpha(theme.palette.text.secondary, 0.12)}`,
            boxShadow: (theme) => `0 30px 60px -30px ${alpha(theme.palette.primary.main, 0.55)}`,
            backdropFilter: 'blur(8px)',
          }}
        >
          <img src={heroUrl} alt="Logotipo UTNG" style={{ width: '74%', maxHeight: '100%', objectFit: 'contain' }} />
        </Box>

        <Box>
          <Typography variant="h4" sx={{ fontWeight: 750, lineHeight: 1.15 }}>
            Controla el avance
            <br />
            de tu institucion
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1.25, maxWidth: 400 }}>
            Centraliza la planeacion, los avances por parcial y las evidencias de
            capacitacion en una sola plataforma.
          </Typography>
        </Box>

        <Stack spacing={2}>
          {BENEFICIOS.map((beneficio) => (
            <Stack key={beneficio.titulo} direction="row" sx={{ gap: 1.75 }}>
              <Box
                sx={{
                  width: 42,
                  height: 42,
                  borderRadius: '13px',
                  color: 'primary.main',
                  backgroundColor: (theme) => alpha(theme.palette.primary.main, 0.12),
                  border: (theme) => `1px solid ${alpha(theme.palette.primary.main, 0.25)}`,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  flexShrink: 0,
                }}
              >
                {beneficio.icono}
              </Box>
              <Box>
                <Typography variant="body2" sx={{ fontWeight: 600 }}>
                  {beneficio.titulo}
                </Typography>
                <Typography variant="body2" color="text.secondary" sx={{ fontSize: '0.8rem', lineHeight: 1.35, mt: 0.1 }}>
                  {beneficio.texto}
                </Typography>
              </Box>
            </Stack>
          ))}
        </Stack>
      </Stack>

      <Typography variant="body2" color="text.secondary" sx={{ position: 'relative', flexShrink: 0 }}>
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
        backgroundImage: 'linear-gradient(135deg, #22D3EE 0%, #A78BFA 100%)',
        color: '#041318',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        boxShadow: '0 12px 24px -12px rgba(34, 211, 238, 0.9)',
      }}
    >
      <GraduationCap size={24} />
    </Box>
  )
}