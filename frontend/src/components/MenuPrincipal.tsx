import AppBar from '@mui/material/AppBar'
import Avatar from '@mui/material/Avatar'
import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Chip from '@mui/material/Chip'
import Divider from '@mui/material/Divider'
import IconButton from '@mui/material/IconButton'
import Menu from '@mui/material/Menu'
import MenuItem from '@mui/material/MenuItem'
import Toolbar from '@mui/material/Toolbar'
import Typography from '@mui/material/Typography'
import { GraduationCap, Home, LogOut, Users } from 'lucide-react'
import { useState, type ReactNode } from 'react'
import { Link as RouterLink, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'

interface OpcionMenu {
  etiqueta: string
  ruta: string
  icono: ReactNode
  roles: string[]
}

const OPCIONES: OpcionMenu[] = [
  { etiqueta: 'Inicio', ruta: '/', icono: <Home size={18} />, roles: ['DOCENTE', 'COORDINADOR', 'DIRECCION'] },
  { etiqueta: 'Usuarios', ruta: '/usuarios', icono: <Users size={18} />, roles: ['COORDINADOR', 'DIRECCION'] },
]

function inicialesDe(nombre: string): string {
  return nombre
    .split(' ')
    .slice(0, 2)
    .map((parte) => parte.charAt(0).toUpperCase())
    .join('')
}

export function MenuPrincipal() {
  const { usuario, logout } = useAuth()
  const navigate = useNavigate()
  const ubicacion = useLocation()
  const [ancla, setAncla] = useState<HTMLElement | null>(null)

  if (!usuario) return null

  const opcionesVisibles = OPCIONES.filter((opcion) => opcion.roles.includes(usuario.rol))

  const cerrarSesion = () => {
    setAncla(null)
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <AppBar
      position="sticky"
      color="transparent"
      elevation={0}
      sx={{
        borderBottom: '1px solid',
        borderColor: 'divider',
        backgroundImage: 'none',
        backgroundColor: 'rgba(255, 255, 255, 0.85)',
        backdropFilter: 'blur(10px)',
      }}
    >
      <Toolbar disableGutters sx={{ px: { xs: 2, sm: 3 }, gap: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.25, mr: 2 }}>
          <Box
            sx={{
              width: 36,
              height: 36,
              borderRadius: '11px',
              backgroundImage: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
              color: '#fff',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <GraduationCap size={20} />
          </Box>
          <Typography variant="subtitle2" sx={{ fontWeight: 700, display: { xs: 'none', sm: 'block' } }}>
            Planeacion Didactica
          </Typography>
        </Box>

        <Box sx={{ display: 'flex', gap: 1, flex: 1 }}>
          {opcionesVisibles.map((opcion) => {
            const activa = ubicacion.pathname === opcion.ruta
            return (
              <Button
                key={opcion.ruta}
                component={RouterLink}
                to={opcion.ruta}
                startIcon={opcion.icono}
                variant={activa ? 'contained' : 'text'}
                color={activa ? 'primary' : 'inherit'}
                size="small"
              >
                {opcion.etiqueta}
              </Button>
            )
          })}
        </Box>

        <Chip
          label={usuario.rol}
          size="small"
          color="primary"
          variant="outlined"
          sx={{ display: { xs: 'none', sm: 'flex' } }}
        />

        <IconButton
          size="small"
          onClick={(e) => setAncla(e.currentTarget)}
          aria-label="Menu de usuario"
          aria-haspopup="menu"
          aria-expanded={ancla ? 'true' : undefined}
        >
          <Avatar
            sx={{
              width: 36,
              height: 36,
              fontSize: '0.85rem',
              fontWeight: 700,
              backgroundImage: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
            }}
          >
            {inicialesDe(usuario.nombre)}
          </Avatar>
        </IconButton>

        <Menu
          anchorEl={ancla}
          open={Boolean(ancla)}
          onClose={() => setAncla(null)}
          slotProps={{ paper: { sx: { mt: 1, minWidth: 230, borderRadius: '14px', p: 1 } } }}
        >
          <Box sx={{ px: 2, py: 1 }}>
            <Typography variant="subtitle2" noWrap>
              {usuario.nombre}
            </Typography>
            <Typography variant="body2" color="text.secondary" noWrap>
              {usuario.email}
            </Typography>
          </Box>
          <Divider sx={{ my: 1 }} />
          <MenuItem onClick={() => { setAncla(null); navigate('/') }}>
            <Home size={18} style={{ marginRight: 10 }} /> Inicio
          </MenuItem>
          {usuario.rol === 'COORDINADOR' || usuario.rol === 'DIRECCION' ? (
            <MenuItem onClick={() => { setAncla(null); navigate('/usuarios') }}>
              <Users size={18} style={{ marginRight: 10 }} /> Usuarios
            </MenuItem>
          ) : null}
          <Divider sx={{ my: 1 }} />
          <MenuItem onClick={cerrarSesion} sx={{ color: 'error.main' }}>
            <LogOut size={18} style={{ marginRight: 10 }} /> Cerrar sesion
          </MenuItem>
        </Menu>
      </Toolbar>
    </AppBar>
  )
}
