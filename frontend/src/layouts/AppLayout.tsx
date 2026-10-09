import AppBar from '@mui/material/AppBar'
import Avatar from '@mui/material/Avatar'
import Box from '@mui/material/Box'
import Breadcrumbs from '@mui/material/Breadcrumbs'
import Chip from '@mui/material/Chip'
import Divider from '@mui/material/Divider'
import Drawer from '@mui/material/Drawer'
import IconButton from '@mui/material/IconButton'
import List from '@mui/material/List'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemIcon from '@mui/material/ListItemIcon'
import ListItemText from '@mui/material/ListItemText'
import Menu from '@mui/material/Menu'
import MenuItem from '@mui/material/MenuItem'
import Toolbar from '@mui/material/Toolbar'
import Tooltip from '@mui/material/Tooltip'
import Typography from '@mui/material/Typography'
import {
  BookOpen,
  CalendarRange,
  ChevronRight,
  ChevronsLeft,
  ClipboardCheck,
  GraduationCap,
  Home,
  ListChecks,
  LogOut,
  Menu as MenuIcon,
  Moon,
  Sun,
  Users,
} from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { useState, type ReactNode } from 'react'
import { Link as RouterLink, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../auth/useAuth'
import { AnimatedPage } from '../components/ui/AnimatedPage'
import { glow, TOKENS } from '../theme'
import { useTema } from '../theme/contextoTema'

interface OpcionNavegacion {
  etiqueta: string
  ruta: string
  icono: LucideIcon
  roles: string[]
}

const OPCIONES_NAVEGACION: OpcionNavegacion[] = [
  {
    etiqueta: 'Inicio',
    ruta: '/',
    icono: Home,
    roles: ['DOCENTE', 'COORDINADOR', 'DIRECCION'],
  },
  { etiqueta: 'Materias', ruta: '/materias', icono: BookOpen, roles: ['COORDINADOR', 'DIRECCION'] },
  { etiqueta: 'Periodos', ruta: '/periodos', icono: CalendarRange, roles: ['COORDINADOR', 'DIRECCION'] },
  { etiqueta: 'Parciales', ruta: '/parciales', icono: ListChecks, roles: ['COORDINADOR', 'DIRECCION'] },
  { etiqueta: 'Planeaciones', ruta: '/planeaciones', icono: ClipboardCheck, roles: ['DOCENTE', 'COORDINADOR', 'DIRECCION'] },

  { etiqueta: 'Usuarios', ruta: '/usuarios', icono: Users, roles: ['COORDINADOR', 'DIRECCION'] },
]

function inicialesDe(nombre: string): string {
  return nombre
    .split(' ')
    .slice(0, 2)
    .map((parte) => parte.charAt(0).toUpperCase())
    .join('')
}

function etiquetaDeRuta(ruta: string): string | null {
  return OPCIONES_NAVEGACION.find((opcion) => opcion.ruta === ruta)?.etiqueta ?? null
}

export function AppLayout({ children }: { children: ReactNode }) {
  const { usuario, logout } = useAuth()
  const { modo, alternarModo } = useTema()
  const navigate = useNavigate()
  const ubicacion = useLocation()
  const [colapsado, setColapsado] = useState(false)
  const [movilAbierto, setMovilAbierto] = useState(false)
  const [ancla, setAncla] = useState<HTMLElement | null>(null)

  if (!usuario) return null

  const opcionesVisibles = OPCIONES_NAVEGACION.filter((opcion) => opcion.roles.includes(usuario.rol))
  const rutaActual = etiquetaDeRuta(ubicacion.pathname)
  const fondo = TOKENS[modo].fondo

  const cerrarSesion = () => {
    setAncla(null)
    logout()
    navigate('/login', { replace: true })
  }

  const contenidoNavegacion = (colapsadoLocal: boolean, cerrarMovil?: () => void) => (
    <>
      <Box
        sx={{
          display: 'flex',
          alignItems: 'center',
          gap: 1.5,
          px: 2,
          minHeight: 72,
          borderBottom: '1px solid',
          borderColor: 'divider',
        }}
      >
        <Box
          sx={{
            width: 40,
            height: 40,
            flexShrink: 0,
            borderRadius: '12px',
            backgroundImage: 'linear-gradient(135deg, #22D3EE 0%, #A78BFA 100%)',
            color: '#041318',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 12px 26px -12px rgba(34, 211, 238, 0.9)',
          }}
        >
          <GraduationCap size={22} />
        </Box>
        {!colapsadoLocal && (
          <Box sx={{ minWidth: 0 }}>
            <Typography variant="subtitle1" sx={{ lineHeight: 1.1, whiteSpace: 'nowrap' }}>
              Planeación Didáctica
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ whiteSpace: 'nowrap' }}>
              UTNG · Medio superior
            </Typography>
          </Box>
        )}
      </Box>

      <List sx={{ px: 1, py: 1.5, flex: 1, overflowY: 'auto', overflowX: 'hidden' }}>
        {opcionesVisibles.map((opcion) => {
          const activa = ubicacion.pathname === opcion.ruta
          const Icono = opcion.icono
          const boton = (
            <ListItemButton
              component={RouterLink}
              to={opcion.ruta}
              onClick={() => cerrarMovil?.()}
              sx={{
                minHeight: 46,
                mb: 0.5,
                borderRadius: 2,
                position: 'relative',
                color: activa ? 'primary.main' : 'text.secondary',
                backgroundColor: activa ? (theme) => `${theme.palette.primary.main}1A` : 'transparent',
                boxShadow: activa ? (theme) => glow(theme.palette.primary.main, '38') : 'none',
                transition: 'all 0.18s ease',
                justifyContent: colapsadoLocal ? 'center' : 'flex-start',
                px: colapsadoLocal ? 1 : 1.5,
                '&:hover': {
                  color: 'primary.main',
                  backgroundColor: (theme) => `${theme.palette.primary.main}12`,
                },
              }}
            >
              {activa && (
                <Box
                  sx={{
                    position: 'absolute',
                    left: 0,
                    top: '50%',
                    transform: 'translateY(-50%)',
                    width: 3,
                    height: 22,
                    borderRadius: 3,
                    backgroundImage: 'linear-gradient(180deg, #22D3EE 0%, #A78BFA 100%)',
                  }}
                />
              )}
              <ListItemIcon sx={{ minWidth: 0, mr: colapsadoLocal ? 0 : 1.5, color: 'inherit' }}>
                <Icono size={19} />
              </ListItemIcon>
              {!colapsadoLocal && <ListItemText primary={opcion.etiqueta} />}
            </ListItemButton>
          )
          return colapsadoLocal ? (
            <Box key={opcion.ruta} sx={{ display: 'flex', justifyContent: 'center' }}>
              <Tooltip title={opcion.etiqueta} placement="right">
                <Box>{boton}</Box>
              </Tooltip>
            </Box>
          ) : (
            <Box key={opcion.ruta}>{boton}</Box>
          )
        })}
      </List>
    </>
  )

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          width: colapsado ? 88 : 264,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: colapsado ? 88 : 264,
            boxSizing: 'border-box',
            bgcolor: TOKENS[modo].superficie,
            borderRight: '1px solid',
            borderColor: 'divider',
            backgroundImage: 'none',
            transition: 'width 0.22s cubic-bezier(0.4, 0, 0.2, 1)',
            overflowX: 'hidden',
          },
        }}
      >
        {contenidoNavegacion(colapsado)}
      </Drawer>

      <Drawer
        variant="temporary"
        open={movilAbierto}
        onClose={() => setMovilAbierto(false)}
        ModalProps={{ keepMounted: true }}
        sx={{
          display: { xs: 'block', md: 'none' },
          '& .MuiDrawer-paper': {
            width: 280,
            boxSizing: 'border-box',
            bgcolor: TOKENS[modo].superficie,
            backgroundImage: 'none',
          },
        }}
      >
        {contenidoNavegacion(false, () => setMovilAbierto(false))}
      </Drawer>

      <Box sx={{ flex: 1, minWidth: 0, display: 'flex', flexDirection: 'column' }}>
        <AppBar
          position="sticky"
          elevation={0}
          color="transparent"
          sx={{
            backgroundColor: `${fondo}A8`,
            borderBottom: '1px solid',
            borderColor: 'divider',
            backgroundImage: 'none',
            backdropFilter: 'blur(12px)',
          }}
        >
          <Toolbar sx={{ gap: 1.25, px: { xs: 1.5, sm: 2.5 }, minHeight: 64 }}>
            <IconButton
              aria-label="Abrir menu"
              onClick={() => setMovilAbierto(true)}
              sx={{ display: { md: 'none' } }}
            >
              <MenuIcon size={20} />
            </IconButton>
            <IconButton
              aria-label={colapsado ? 'Expandir menu lateral' : 'Colapsar menu lateral'}
              onClick={() => setColapsado((actual) => !actual)}
              sx={{ display: { xs: 'none', md: 'inline-flex' } }}
            >
              <ChevronsLeft
                size={20}
                style={{ transform: colapsado ? 'rotate(180deg)' : 'none', transition: 'transform 0.2s ease' }}
              />
            </IconButton>

            <Breadcrumbs
              aria-label="Ruta de navegacion"
              separator={<ChevronRight size={15} />}
              sx={{ mr: 'auto', '& .MuiBreadcrumbs-separator': { mx: 0.25 } }}
            >
              <Typography
                component={RouterLink}
                to="/"
                variant="body2"
                sx={{ color: 'text.secondary', fontWeight: 600, textDecoration: 'none', '&:hover': { color: 'primary.main' } }}
              >
                Inicio
              </Typography>
              {rutaActual && rutaActual !== 'Inicio' && (
                <Typography variant="body2" color="text.primary" sx={{ fontWeight: 600 }}>
                  {rutaActual}
                </Typography>
              )}
            </Breadcrumbs>

            <Tooltip title={modo === 'dark' ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro'}>
              <IconButton onClick={alternarModo} aria-label="Cambiar tema">
                {modo === 'dark' ? <Sun size={19} /> : <Moon size={19} />}
              </IconButton>
            </Tooltip>

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
                  backgroundImage: 'linear-gradient(135deg, #22D3EE 0%, #A78BFA 100%)',
                  color: '#041318',
                }}
              >
                {inicialesDe(usuario.nombre)}
              </Avatar>
            </IconButton>

            <Menu
              anchorEl={ancla}
              open={Boolean(ancla)}
              onClose={() => setAncla(null)}
              slotProps={{ paper: { sx: { mt: 1.25, minWidth: 240, borderRadius: '16px', p: 1 } } }}
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
              <Box sx={{ px: 2, py: 1 }}>
                <Chip size="small" color="info" variant="outlined" label={usuario.rol} sx={{ textTransform: 'uppercase' }} />
              </Box>
              <Divider sx={{ my: 1 }} />
              <MenuItem onClick={cerrarSesion} sx={{ color: 'error.main' }}>
                <LogOut size={18} style={{ marginRight: 10 }} /> Cerrar sesión
              </MenuItem>
            </Menu>
          </Toolbar>
        </AppBar>

        <Box component="main" sx={{ flex: 1, minWidth: 0 }}>
          <AnimatedPage key={ubicacion.pathname}>{children}</AnimatedPage>
        </Box>

        <Box
          component="footer"
          sx={{
            borderTop: '1px solid',
            borderColor: 'divider',
            px: 3,
            py: 2.5,
            display: 'flex',
            flexDirection: { xs: 'column', sm: 'row' },
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: 1,
          }}
        >
          <Typography variant="body2" color="text.secondary">
            Sistema de seguimiento de planeación didáctica · UTNG
          </Typography>
          <Typography variant="caption" color="text.secondary" sx={{ opacity: 0.7 }}>
            © 2026 · Desarrollo Web Integral
          </Typography>
        </Box>
      </Box>
    </Box>
  )
}