import Box from '@mui/material/Box'
import Card from '@mui/material/Card'
import Chip from '@mui/material/Chip'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { alpha } from '@mui/material/styles'
import { ArrowUpRight, BookOpen, CalendarRange, ClipboardCheck, ListChecks, ShieldCheck, Users } from 'lucide-react'
import type { LucideIcon } from 'lucide-react'
import { Link as RouterLink } from 'react-router-dom'
import { listarMaterias, listarParciales, listarPeriodos } from '../api/catalogo'
import { useAuth } from '../auth/useAuth'
import { GraficaDistribucion } from '../components/catalogo/GraficaDistribucion'
import { TarjetaEstadistica } from '../components/catalogo/TarjetaEstadistica'
import { useConteosCatalogo } from '../hooks/useConteosCatalogo'

interface AccesoRapido {
  ruta: string
  titulo: string
  descripcion: string
  icono: LucideIcon
  roles: string[]
}

const ACCESOS: AccesoRapido[] = [
  { ruta: '/materias', titulo: 'Materias', descripcion: 'Administra el catalogo de materias y su estado.', icono: BookOpen, roles: ['COORDINADOR', 'DIRECCION'] },
  { ruta: '/periodos', titulo: 'Periodos', descripcion: 'Gestiona los periodos escolares y su activacion.', icono: CalendarRange, roles: ['COORDINADOR', 'DIRECCION'] },
  { ruta: '/parciales', titulo: 'Parciales', descripcion: 'Configura los parciales por periodo.', icono: ListChecks, roles: ['COORDINADOR', 'DIRECCION'] },
  { ruta: '/usuarios', titulo: 'Usuarios', descripcion: 'Cuentas y roles del personal academico.', icono: Users, roles: ['COORDINADOR', 'DIRECCION'] },
  { ruta: '/planeaciones', titulo: 'Planeaciones', descripcion: 'Registra y da seguimiento a tus planeaciones didacticas.', icono: ClipboardCheck, roles: ['DOCENTE', 'COORDINADOR', 'DIRECCION'] },
]

function inicialesDe(nombre: string): string {
  return nombre
    .split(' ')
    .slice(0, 2)
    .map((parte) => parte.charAt(0).toUpperCase())
    .join('')
}

function saludoDe(): string {
  const hora = new Date().getHours()
  if (hora < 12) return 'Buenos dias'
  if (hora < 19) return 'Buenas tardes'
  return 'Buenas noches'
}

function fechaDe(): string {
  return new Date().toLocaleDateString('es-MX', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' })
}

export function HomePage() {
  const { usuario } = useAuth()

  const materias = useConteosCatalogo(listarMaterias, 0)
  const periodos = useConteosCatalogo(listarPeriodos, 0)
  const parciales = useConteosCatalogo(listarParciales, 0)

  if (!usuario) return null

  const esAdmin = usuario.rol === 'COORDINADOR' || usuario.rol === 'DIRECCION'
  const accesosVisibles = ACCESOS.filter((acceso) => acceso.roles.includes(usuario.rol))

  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 'lg', mx: 'auto' }}>
      <Stack spacing={3}>
        <Card
          sx={{
            position: 'relative',
            overflow: 'hidden',
            borderRadius: '24px',
            p: { xs: 2.5, sm: 3.5 },
            backgroundImage: (theme) =>
              `linear-gradient(135deg, ${alpha(theme.palette.primary.main, 0.16)} 0%, ${alpha('#A78BFA', 0.1)} 55%, ${alpha(theme.palette.primary.main, 0.05)} 100%)`,
          }}
        >
          <Box
            sx={{
              position: 'absolute',
              width: 260,
              height: 260,
              borderRadius: '50%',
              top: -130,
              right: -70,
              background: (theme) => `radial-gradient(circle, ${alpha(theme.palette.primary.main, 0.25)} 0%, transparent 70%)`,
            }}
          />
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2.5} sx={{ position: 'relative', alignItems: { xs: 'flex-start', sm: 'center' } }}>
            <Box
              sx={{
                width: 58,
                height: 58,
                borderRadius: '18px',
                flexShrink: 0,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: '1.15rem',
                fontWeight: 750,
                color: '#041318',
                backgroundImage: 'linear-gradient(135deg, #22D3EE 0%, #A78BFA 100%)',
                boxShadow: '0 14px 30px -14px rgba(34, 211, 238, 0.9)',
              }}
            >
              {inicialesDe(usuario.nombre)}
            </Box>
            <Box sx={{ flex: 1 }}>
              <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 600 }}>
                {saludoDe()} · {fechaDe()}
              </Typography>
              <Typography variant="h4" sx={{ mt: 0.25 }}>
                {usuario.nombre}
              </Typography>
              <Typography variant="body2" color="text.secondary">
                Esto es lo que esta pasando en el sistema:
              </Typography>
            </Box>
            <Stack direction="row" spacing={1.25} sx={{ flexWrap: 'wrap' }}>
              <Chip size="small" color="info" icon={<ShieldCheck size={15} />} label={usuario.rol} sx={{ textTransform: 'uppercase' }} />
              <Chip size="small" color="success" label="Cuenta activa" />
            </Stack>
          </Stack>
        </Card>

        {esAdmin && (
          <Box sx={{ display: 'flex', flexDirection: { xs: 'column', md: 'row' }, gap: 2 }}>
            <Box
              sx={{
                flex: 1,
                display: 'grid',
                gap: 2,
                gridTemplateColumns: { xs: '1fr', sm: 'repeat(3, 1fr)' },
              }}
            >
              <TarjetaEstadistica etiqueta="Materias registradas" valor={materias.activos + materias.inactivos} icono={<BookOpen size={22} />} variedad="primario" />
              <TarjetaEstadistica etiqueta="Periodos escolares" valor={periodos.activos + periodos.inactivos} icono={<CalendarRange size={22} />} variedad="exito" />
              <TarjetaEstadistica etiqueta="Parciales configurados" valor={parciales.activos + parciales.inactivos} icono={<ListChecks size={22} />} variedad="neutro" />
            </Box>
            <GraficaDistribucion activos={materias.activos} inactivos={materias.inactivos} />
          </Box>
        )}

        <Box>
          <Typography variant="h6">Accesos rapidos</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Accede a los modulos que corresponden a tu rol en la plataforma.
          </Typography>
          <Box
            sx={{
              display: 'grid',
              gap: 2,
              gridTemplateColumns: { xs: '1fr', sm: 'repeat(auto-fit, minmax(250px, 1fr))' },
            }}
          >
            {accesosVisibles.map((acceso) => {
              const Icono = acceso.icono
              return (
                <Card
                  key={acceso.ruta}
                  component={RouterLink}
                  to={acceso.ruta}
                  sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 1.5,
                    p: 2.5,
                    textDecoration: 'none',
                    transition: 'transform 0.2s ease, box-shadow 0.2s ease',
                    '&:hover': {
                      transform: 'translateY(-3px)',
                      boxShadow: (theme) => `0 22px 40px -26px ${alpha(theme.palette.primary.main, 0.6)}`,
                    },
                  }}
                >
                  <Box
                    sx={{
                      width: 44,
                      height: 44,
                      borderRadius: '13px',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: 'primary.main',
                      backgroundColor: (theme) => alpha(theme.palette.primary.main, 0.12),
                    }}
                  >
                    <Icono size={21} />
                  </Box>
                  <Box sx={{ flex: 1 }}>
                    <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
                      {acceso.titulo}
                    </Typography>
                    <Typography variant="body2" color="text.secondary">
                      {acceso.descripcion}
                    </Typography>
                  </Box>
                  <Stack direction="row" sx={{ alignItems: 'center', gap: 0.5, color: 'primary.main' }}>
                    <Typography variant="body2" sx={{ fontWeight: 600 }}>
                      Abrir
                    </Typography>
                    <ArrowUpRight size={17} />
                  </Stack>
                </Card>
              )
            })}
          </Box>
        </Box>
      </Stack>
    </Box>
  )
}