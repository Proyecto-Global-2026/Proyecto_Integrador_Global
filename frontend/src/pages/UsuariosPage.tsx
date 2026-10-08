import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormHelperText,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
  Box,
  Chip,
  CircularProgress,
} from '@mui/material'
import { useCallback, useEffect, useState } from 'react'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useForm } from 'react-hook-form'
import {
  actualizarUsuario,
  crearUsuario,
  desactivarUsuario,
  listarRoles,
  listarUsuarios,
  reactivarUsuario,
} from '../api/usuarios'
import type { Paginado, Rol, Usuario } from '../types/usuario'
import { useAuth } from '../auth/useAuth'

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
  const [error, setError] = useState<string | null>(null)
  const [dialogoCrear, setDialogoCrear] = useState(false)
  const [dialogoEditar, setDialogoEditar] = useState<Usuario | null>(null)

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
      setError(null)
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
        if (!cancelado) setError('Error al cargar usuarios')
      } finally {
        if (!cancelado) setCargando(false)
      }
    }
    void cargarUsuarios()
    return () => {
      cancelado = true
    }
  }, [page, rowsPerPage, filtroRol, filtroActivo])

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
      await cargar()
    } catch {
      setError('Error al crear usuario')
    }
  })

  const guardarEditar = formEditar.handleSubmit(async (val) => {
    if (!dialogoEditar) return
    try {
      await actualizarUsuario(dialogoEditar.id, val)
      setDialogoEditar(null)
      await cargar()
    } catch {
      setError('Error al actualizar usuario')
    }
  })

  const toggleActivo = async (u: Usuario) => {
    try {
      if (u.activo) await desactivarUsuario(u.id)
      else await reactivarUsuario(u.id)
      await cargar()
    } catch {
      setError('Error al cambiar estado')
    }
  }

  const total = paginado?.totalElementos ?? 0

  return (
    <Box sx={{ p: 3 }}>
      <Stack
        direction="row"
        sx={{ justifyContent: 'space-between', alignItems: 'center', mb: 2 }}
      >
        <Typography variant="h5">Usuarios y roles</Typography>
        {puedeEditar && (
          <Button variant="contained" onClick={abrirCrear}>
            Nuevo usuario
          </Button>
        )}
      </Stack>

      <Paper sx={{ p: 2, mb: 2 }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
          <FormControl sx={{ minWidth: 200 }}>
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
          <FormControl sx={{ minWidth: 200 }}>
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
        </Stack>
      </Paper>

      {error && (
        <Box sx={{ mb: 2 }}>
          <Typography color="error">{error}</Typography>
        </Box>
      )}

      <TableContainer component={Paper}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Nombre</TableCell>
              <TableCell>Correo</TableCell>
              <TableCell>Rol</TableCell>
              <TableCell>Estado</TableCell>
              {puedeEditar && <TableCell align="right">Acciones</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {cargando && (
              <TableRow>
                <TableCell colSpan={puedeEditar ? 5 : 4} align="center">
                  <Stack
                    direction="row"
                    spacing={2}
                    sx={{ justifyContent: 'center', py: 2 }}
                  >
                    <CircularProgress size={24} />
                    <Typography variant="body2" color="text.secondary">
                      Cargando usuarios...
                    </Typography>
                  </Stack>
                </TableCell>
              </TableRow>
            )}
            {!cargando && usuarios.length === 0 && (
              <TableRow>
                <TableCell colSpan={puedeEditar ? 5 : 4} align="center">
                  <Typography variant="body2" color="text.secondary" sx={{ py: 2 }}>
                    No se encontraron usuarios
                  </Typography>
                </TableCell>
              </TableRow>
            )}
            {usuarios.map((u) => (
              <TableRow key={u.id}>
                <TableCell>{u.nombre}</TableCell>
                <TableCell>{u.email}</TableCell>
                <TableCell>
                  <Chip label={u.rol} size="small" />
                </TableCell>
                <TableCell>
                  <Chip
                    label={u.activo ? 'Activo' : 'Inactivo'}
                    color={u.activo ? 'success' : 'default'}
                    size="small"
                  />
                </TableCell>
                {puedeEditar && (
                  <TableCell align="right">
                    <Stack direction="row" spacing={1} sx={{ justifyContent: 'flex-end' }}>
                      <Button size="small" onClick={() => abrirEditar(u)}>
                        Editar
                      </Button>
                      <Button
                        size="small"
                        color={u.activo ? 'warning' : 'success'}
                        onClick={() => toggleActivo(u)}
                      >
                        {u.activo ? 'Desactivar' : 'Reactivar'}
                      </Button>
                    </Stack>
                  </TableCell>
                )}
              </TableRow>
            ))}
          </TableBody>
        </Table>
        <TablePagination
          component="div"
          count={total}
          page={page}
          onPageChange={(_, p) => setPage(p)}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={(e) => {
            setRowsPerPage(parseInt(e.target.value, 10))
            setPage(0)
          }}
          rowsPerPageOptions={[10, 20, 50]}
        />
      </TableContainer>

      <Dialog open={dialogoCrear} onClose={() => setDialogoCrear(false)} maxWidth="sm" fullWidth>
        <form onSubmit={guardarCrear} noValidate>
          <DialogTitle>Nuevo usuario</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
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
          <DialogActions>
            <Button onClick={() => setDialogoCrear(false)}>Cancelar</Button>
            <Button type="submit" variant="contained" disabled={formCrear.formState.isSubmitting}>
              Guardar
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      <Dialog open={!!dialogoEditar} onClose={() => setDialogoEditar(null)} maxWidth="sm" fullWidth>
        <form onSubmit={guardarEditar} noValidate>
          <DialogTitle>Editar usuario</DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 1 }}>
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
          <DialogActions>
            <Button onClick={() => setDialogoEditar(null)}>Cancelar</Button>
            <Button type="submit" variant="contained" disabled={formEditar.formState.isSubmitting}>
              Guardar
            </Button>
          </DialogActions>
        </form>
      </Dialog>
    </Box>
  )
}
