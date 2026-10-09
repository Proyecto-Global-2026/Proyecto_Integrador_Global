import { zodResolver } from '@hookform/resolvers/zod'
import {
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormHelperText,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import { alpha } from '@mui/material/styles'
import { Pencil, Plus, Power, PowerOff, Users } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { sileo } from 'sileo'
import { z } from 'zod'
import {
  actualizarUsuario,
  crearUsuario,
  desactivarUsuario,
  listarRoles,
  listarUsuarios,
  reactivarUsuario,
} from '../api/usuarios'
import { useAuth } from '../auth/useAuth'
import { TablaCatalogo, type ColumnaCatalogo } from '../components/TablaCatalogo'
import { BotonCabecera } from '../components/catalogo/BotonCabecera'
import { ChipEstado } from '../components/catalogo/ChipEstado'
import { ConfirmarAccion } from '../components/catalogo/ConfirmarAccion'
import { EncabezadoPagina } from '../components/catalogo/EncabezadoPagina'
import { PanelFiltros } from '../components/catalogo/PanelFiltros'
import { PanelResumen } from '../components/catalogo/PanelResumen'
import { useConteosCatalogo } from '../hooks/useConteosCatalogo'
import type { Paginado, Rol, Usuario } from '../types/usuario'

const schemaCrear = z.object({
  nombre: z.string().min(1, 'El nombre es obligatorio').max(120, 'Máximo 120 caracteres'),
  email: z
    .string()
    .min(1, 'El correo es obligatorio')
    .email('Correo inválido')
    .max(180, 'Máximo 180 caracteres'),
  password: z.string().min(8, 'Mínimo 8 caracteres').max(72, 'Máximo 72 caracteres'),
  rol: z.string().min(1, 'El rol es obligatorio'),
})

const schemaEditar = z.object({
  nombre: z.string().min(1, 'El nombre es obligatorio').max(120, 'Máximo 120 caracteres'),
  email: z
    .string()
    .min(1, 'El correo es obligatorio')
    .email('Correo inválido')
    .max(180, 'Máximo 180 caracteres'),
  rol: z.string().min(1, 'El rol es obligatorio'),
})

type FormCrear = z.infer<typeof schemaCrear>
type FormEditar = z.infer<typeof schemaEditar>

const ESTADOS = [
  { label: 'Activos', value: 'true' },
  { label: 'Inactivos', value: 'false' },
  { label: 'Todos', value: '' },
] as const

function inicialesDe(nombre: string): string {
  return nombre
    .split(' ')
    .slice(0, 2)
    .map((parte) => parte.charAt(0).toUpperCase())
    .join('')
}

function AvatarIniciales({ nombre, tamano = 34 }: { nombre: string; tamano?: number }) {
  return (
    <Box
      sx={{
        width: tamano,
        height: tamano,
        flexShrink: 0,
        borderRadius: '11px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        fontSize: tamano * 0.42,
        fontWeight: 750,
        color: '#041318',
        backgroundImage: 'linear-gradient(135deg, #22D3EE 0%, #A78BFA 100%)',
      }}
    >
      {inicialesDe(nombre)}
    </Box>
  )
}

export function UsuariosPage() {
  const { usuario: authUsuario } = useAuth()
  const puedeEditar = authUsuario?.rol === 'DIRECCION'

  const [usuarios, setUsuarios] = useState<Usuario[]>([])
  const [roles, setRoles] = useState<Rol[]>([])
  const [paginado, setPaginado] = useState<Paginado<Usuario> | null>(null)
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(20)
  const [filtroRol, setFiltroRol] = useState('')
  const [filtroActivo, setFiltroActivo] = useState('')
  const [cargando, setCargando] = useState(false)
  const [dialogoCrear, setDialogoCrear] = useState(false)
  const [dialogoEditar, setDialogoEditar] = useState<Usuario | null>(null)
  const [porConfirmar, setPorConfirmar] = useState<Usuario | null>(null)
  const [version, setVersion] = useState(0)

  const { activos, inactivos } = useConteosCatalogo(
    useCallback((params) => listarUsuarios({ rol: null, ...params }), []),
    version,
  )

  const formCrear = useForm<FormCrear>({
    resolver: zodResolver(schemaCrear),
    defaultValues: { nombre: '', email: '', password: '', rol: '' },
  })

  const formEditar = useForm<FormEditar>({
    resolver: zodResolver(schemaEditar),
    defaultValues: { nombre: '', email: '', rol: '' },
  })

  const cargar = useCallback(async () => {
    const activo = filtroActivo === '' ? null : filtroActivo === 'true'
    const datos = await listarUsuarios({
      rol: filtroRol || null,
      activo,
      page,
      size: rowsPerPage,
    })
    setUsuarios(datos.content)
    setPaginado(datos)
  }, [page, rowsPerPage, filtroRol, filtroActivo])

  useEffect(() => {
    let cancelado = false
    const cargarRoles = async () => {
      try {
        const datos = await listarRoles()
        if (!cancelado) setRoles(datos)
      } catch {
        if (!cancelado) setRoles([])
      }
    }
    void cargarRoles()
    return () => {
      cancelado = true
    }
  }, [])

  useEffect(() => {
    let cancelado = false
    const cargarUsuarios = async () => {
      setCargando(true)
      try {
        const datos = await listarUsuarios({
          rol: filtroRol || null,
          activo: filtroActivo === '' ? null : filtroActivo === 'true',
          page,
          size: rowsPerPage,
        })
        if (!cancelado) {
          setUsuarios(datos.content)
          setPaginado(datos)
        }
      } catch {
        if (!cancelado) sileo.error({ title: 'No se pudieron cargar los usuarios' })
      } finally {
        if (!cancelado) setCargando(false)
      }
    }
    void cargarUsuarios()
    return () => {
      cancelado = true
    }
  }, [page, rowsPerPage, filtroRol, filtroActivo])

  const recargar = () => {
    void cargar()
    setVersion((v) => v + 1)
  }

  const abrirCrear = () => {
    formCrear.reset({ nombre: '', email: '', password: '', rol: roles[0]?.nombre || '' })
    setDialogoCrear(true)
  }

  const abrirEditar = (u: Usuario) => {
    formEditar.reset({ nombre: u.nombre, email: u.email, rol: u.rol })
    setDialogoEditar(u)
  }

  const guardarCrear = formCrear.handleSubmit(async (val) => {
    try {
      await crearUsuario(val)
      setDialogoCrear(false)
      sileo.success({ title: 'Usuario creado' })
      await cargar()
      setVersion((v) => v + 1)
    } catch {
      sileo.error({ title: 'No se pudo crear el usuario' })
    }
  })

  const guardarEditar = formEditar.handleSubmit(async (val) => {
    if (!dialogoEditar) return
    try {
      await actualizarUsuario(dialogoEditar.id, val)
      setDialogoEditar(null)
      sileo.success({ title: 'Usuario actualizado' })
      await cargar()
    } catch {
      sileo.error({ title: 'No se pudo actualizar el usuario' })
    }
  })

  const confirmarCambioEstado = async () => {
    if (!porConfirmar) return
    try {
      if (porConfirmar.activo) {
        await desactivarUsuario(porConfirmar.id)
        sileo.success({ title: 'Usuario desactivado' })
      } else {
        await reactivarUsuario(porConfirmar.id)
        sileo.success({ title: 'Usuario reactivado' })
      }
      setPorConfirmar(null)
      await cargar()
      setVersion((v) => v + 1)
    } catch {
      sileo.error({ title: 'No se pudo cambiar el estado' })
    }
  }

  const total = paginado?.totalElementos ?? 0

  const columnas: ColumnaCatalogo<Usuario>[] = [
    {
      encabezado: 'Usuario',
      render: (u) => (
        <Stack direction="row" spacing={1.5} sx={{ alignItems: 'center' }}>
          <AvatarIniciales nombre={u.nombre} />
          <Box>
            <Typography variant="body2" sx={{ fontWeight: 600 }}>
              {u.nombre}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {u.email}
            </Typography>
          </Box>
        </Stack>
      ),
    },
    {
      encabezado: 'Rol',
      render: (u) => <Chip size="small" color="info" variant="outlined" label={u.rol} sx={{ textTransform: 'uppercase' }} />,
    },
    {
      encabezado: 'Estado',
      render: (u) => <ChipEstado activo={u.activo} />,
    },
    {
      encabezado: 'Proveedor',
      render: (u) => (
        <Typography variant="body2" color="text.secondary">
          {u.proveedor ?? '—'}
        </Typography>
      ),
    },
  ]

  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 1200, mx: 'auto' }}>
      <EncabezadoPagina
        titulo="Usuarios y roles"
        descripcion="Administra las cuentas y roles del personal académico."
        icono={<Users size={26} />}
        accion={
          puedeEditar ? (
            <BotonCabecera startIcon={<Plus size={18} />} onClick={abrirCrear}>
              Nuevo usuario
            </BotonCabecera>
          ) : undefined
        }
      />

      <PanelResumen activos={activos} inactivos={inactivos} />

      <PanelFiltros onRecargar={recargar}>
        <FormControl size="small" sx={{ minWidth: 200 }}>
          <InputLabel>Rol</InputLabel>
          <Select
            value={filtroRol}
            label="Rol"
            onChange={(e) => {
              setPage(0)
              setFiltroRol(e.target.value)
            }}
          >
            <MenuItem value="">Todos</MenuItem>
            {roles.map((r) => (
              <MenuItem key={r.id} value={r.nombre}>
                {r.nombre}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        <FormControl size="small" sx={{ minWidth: 180 }}>
          <InputLabel>Estado</InputLabel>
          <Select
            value={filtroActivo}
            label="Estado"
            onChange={(e) => {
              setPage(0)
              setFiltroActivo(e.target.value)
            }}
          >
            {ESTADOS.map((e) => (
              <MenuItem key={e.value} value={e.value}>
                {e.label}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </PanelFiltros>

      <TablaCatalogo
        columnas={columnas}
        filas={usuarios}
        cargando={cargando}
        total={total}
        pagina={page}
        tamanoPagina={rowsPerPage}
        onCambiarPagina={setPage}
        onCambiarTamano={setRowsPerPage}
        claveFila={(u) => u.id}
        mensajeVacio="No se encontraron usuarios"
        detalleVacio="Ajusta los filtros o registra un nuevo usuario para comenzar."
        acciones={
          puedeEditar
            ? (u) => (
                <>
                  <Tooltip title="Editar">
                    <IconButton size="small" onClick={() => abrirEditar(u)} sx={{ color: 'primary.main' }}>
                      <Pencil size={17} />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title={u.activo ? 'Desactivar' : 'Reactivar'}>
                    <IconButton
                      size="small"
                      onClick={() => setPorConfirmar(u)}
                      sx={{ color: u.activo ? 'warning.main' : 'success.main' }}
                    >
                      {u.activo ? <PowerOff size={17} /> : <Power size={17} />}
                    </IconButton>
                  </Tooltip>
                </>
              )
            : undefined
        }
      />

      <Dialog open={dialogoCrear} onClose={() => setDialogoCrear(false)} maxWidth="sm" fullWidth>
        <form onSubmit={guardarCrear} noValidate>
          <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1.25, pb: 0 }}>
            <Box
              sx={{
                width: 40,
                height: 40,
                borderRadius: '12px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'primary.main',
                backgroundColor: (theme) => alpha(theme.palette.primary.main, 0.12),
              }}
            >
              <Users size={20} />
            </Box>
            Nuevo usuario
          </DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 2 }}>
              <Typography variant="overline" color="text.secondary">
                Datos personales
              </Typography>
              <TextField
                label="Nombre"
                fullWidth
                {...formCrear.register('nombre')}
                error={!!formCrear.formState.errors.nombre}
                helperText={formCrear.formState.errors.nombre?.message}
              />
              <TextField
                label="Correo electrónico"
                type="email"
                fullWidth
                {...formCrear.register('email')}
                error={!!formCrear.formState.errors.email}
                helperText={formCrear.formState.errors.email?.message}
              />
              <Typography variant="overline" color="text.secondary">
                Acceso y rol
              </Typography>
              <TextField
                label="Contraseña"
                type="password"
                fullWidth
                {...formCrear.register('password')}
                error={!!formCrear.formState.errors.password}
                helperText={formCrear.formState.errors.password?.message}
              />
              <FormControl fullWidth error={!!formCrear.formState.errors.rol}>
                <InputLabel>Rol</InputLabel>
                <Select label="Rol" {...formCrear.register('rol')} defaultValue="">
                  {roles.map((r) => (
                    <MenuItem key={r.id} value={r.nombre}>
                      {r.nombre}
                    </MenuItem>
                  ))}
                </Select>
                <FormHelperText>{formCrear.formState.errors.rol?.message}</FormHelperText>
              </FormControl>
            </Stack>
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 2 }}>
            <Button onClick={() => setDialogoCrear(false)} color="inherit">
              Cancelar
            </Button>
            <Button type="submit" variant="contained" disabled={formCrear.formState.isSubmitting}>
              Guardar
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      <Dialog open={!!dialogoEditar} onClose={() => setDialogoEditar(null)} maxWidth="sm" fullWidth>
        <form onSubmit={guardarEditar} noValidate>
          <DialogTitle sx={{ display: 'flex', alignItems: 'center', gap: 1.25, pb: 0 }}>
            <Box
              sx={{
                width: 40,
                height: 40,
                borderRadius: '12px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'primary.main',
                backgroundColor: (theme) => alpha(theme.palette.primary.main, 0.12),
              }}
            >
              <Pencil size={20} />
            </Box>
            Editar usuario
          </DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 2 }}>
              <TextField
                label="Nombre"
                fullWidth
                {...formEditar.register('nombre')}
                error={!!formEditar.formState.errors.nombre}
                helperText={formEditar.formState.errors.nombre?.message}
              />
              <TextField
                label="Correo electrónico"
                type="email"
                fullWidth
                {...formEditar.register('email')}
                error={!!formEditar.formState.errors.email}
                helperText={formEditar.formState.errors.email?.message}
              />
              <FormControl fullWidth error={!!formEditar.formState.errors.rol}>
                <InputLabel>Rol</InputLabel>
                <Select label="Rol" {...formEditar.register('rol')} defaultValue={dialogoEditar?.rol}>
                  {roles.map((r) => (
                    <MenuItem key={r.id} value={r.nombre}>
                      {r.nombre}
                    </MenuItem>
                  ))}
                </Select>
                <FormHelperText>{formEditar.formState.errors.rol?.message}</FormHelperText>
              </FormControl>
            </Stack>
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 2 }}>
            <Button onClick={() => setDialogoEditar(null)} color="inherit">
              Cancelar
            </Button>
            <Button type="submit" variant="contained" disabled={formEditar.formState.isSubmitting}>
              Guardar
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      <ConfirmarAccion
        abierto={porConfirmar !== null}
        titulo={porConfirmar?.activo ? 'Desactivar usuario' : 'Reactivar usuario'}
        mensaje={
          porConfirmar?.activo
            ? `¿Deseas desactivar a "${porConfirmar?.nombre}"? Perderá el acceso al sistema.`
            : `¿Deseas reactivar a "${porConfirmar?.nombre}"? Volverá a tener acceso.`
        }
        textoConfirmar={porConfirmar?.activo ? 'Desactivar' : 'Reactivar'}
        color={porConfirmar?.activo ? 'warning' : 'success'}
        onConfirmar={confirmarCambioEstado}
        onCerrar={() => setPorConfirmar(null)}
      />
    </Box>
  )
}