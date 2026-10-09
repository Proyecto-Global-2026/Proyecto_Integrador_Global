import LogoutIcon from '@mui/icons-material/Logout'
import { Avatar, Box, Button, Card, CardContent, Chip, Divider, Stack, Typography } from '@mui/material'
import { BookOpen, CalendarRange, CheckCircle2, ListChecks, Mail, ShieldCheck, Users } from 'lucide-react'
import { Link as RouterLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

function inicialesDe(nombre: string): string {
  return nombre
    .split(' ')
    .slice(0, 2)
    .map((parte) => parte.charAt(0).toUpperCase())
    .join('')
}

export function HomePage() {
  const { usuario, logout } = useAuth()
  const navigate = useNavigate()

  if (!usuario) return null

  const cerrarSesion = () => {
    logout()
    navigate('/login', { replace: true })
  }

  const puedeVerUsuarios = usuario.rol === 'COORDINADOR' || usuario.rol === 'DIRECCION'

  return (
    <Box className="pagina-centrada">
      <Card className="tarjeta-login" elevation={0} sx={{ maxWidth: 520, overflow: 'hidden' }}>
        <Box
          sx={{
            backgroundImage: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
            px: 4,
            py: 3.5,
            color: '#fff',
          }}
        >
          <Stack direction="row" sx={{ alignItems: 'center', gap: 2 }}>
            <Avatar
              sx={{
                width: 64,
                height: 64,
                fontSize: '1.5rem',
                fontWeight: 700,
                background: 'rgba(255, 255, 255, 0.22)',
                border: '2px solid rgba(255, 255, 255, 0.45)',
              }}
            >
              {inicialesDe(usuario.nombre)}
            </Avatar>
            <Box>
              <Typography variant="body2" sx={{ opacity: 0.85 }}>
                Sesion iniciada
              </Typography>
              <Typography variant="h6" sx={{ color: '#fff' }}>
                {usuario.nombre}
              </Typography>
            </Box>
          </Stack>
        </Box>

        <CardContent sx={{ p: { xs: 3, sm: 4 } }}>
          <Stack spacing={2}>
            <Stack direction="row" sx={{ alignItems: 'center', gap: 1.5 }}>
              <Chip
                label={usuario.rol}
                color="primary"
                variant="outlined"
                icon={<ShieldCheck size={15} />}
              />
              <Chip
                label="Cuenta activa"
                color="success"
                variant="outlined"
                icon={<CheckCircle2 size={15} />}
              />
            </Stack>

            <Divider />

            <Stack direction="row" sx={{ alignItems: 'center', gap: 1.5, color: 'text.secondary' }}>
              <Mail size={18} />
              <Typography variant="body2">{usuario.email}</Typography>
            </Stack>

            {puedeVerUsuarios && (
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={1.5}>
                <Button
                  variant="outlined"
                  size="large"
                  startIcon={<Users size={18} />}
                  component={RouterLink}
                  to="/usuarios"
                  sx={{ flex: 1 }}
                >
                  Usuarios
                </Button>
                <Button
                  variant="outlined"
                  size="large"
                  startIcon={<BookOpen size={18} />}
                  component={RouterLink}
                  to="/materias"
                  sx={{ flex: 1 }}
                >
                  Materias
                </Button>
                <Button
                  variant="outlined"
                  size="large"
                  startIcon={<CalendarRange size={18} />}
                  component={RouterLink}
                  to="/periodos"
                  sx={{ flex: 1 }}
                >
                  Periodos
                </Button>
                <Button
                  variant="outlined"
                  size="large"
                  startIcon={<ListChecks size={18} />}
                  component={RouterLink}
                  to="/parciales"
                  sx={{ flex: 1 }}
                >
                  Parciales
                </Button>
              </Stack>
            )}

            <AlertSesion />

            <Button
              variant="outlined"
              size="large"
              fullWidth
              startIcon={<LogoutIcon />}
              onClick={cerrarSesion}
              sx={{ py: 1.15 }}
            >
              Cerrar sesion
            </Button>
          </Stack>
        </CardContent>
      </Card>
    </Box>
  )
}

function AlertSesion() {
  return (
    <Box
      sx={{
        display: 'flex',
        gap: 1.5,
        alignItems: 'flex-start',
        p: 1.75,
        borderRadius: '12px',
        border: '1px solid #d1fae5',
        background: '#ecfdf5',
      }}
    >
      <CheckCircle2 size={20} color="#10b981" style={{ flexShrink: 0, marginTop: 2 }} />
      <Typography variant="body2" sx={{ color: '#065f46' }}>
        Tu token JWT quedo almacenado de forma segura y se adjunta automaticamente a cada
        peticion hacia la API.
      </Typography>
    </Box>
  )
}
