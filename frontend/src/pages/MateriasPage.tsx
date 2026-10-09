import { zodResolver } from '@hookform/resolvers/zod'
import { alpha } from '@mui/material/styles'
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  IconButton,
  InputAdornment,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import { BookOpen, Pencil, Plus, Power, PowerOff, Search, X } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { sileo } from 'sileo'
import { z } from 'zod'
import {
  actualizarMateria,
  crearMateria,
  desactivarMateria,
  listarMaterias,
  reactivarMateria,
} from '../api/catalogo'
import { TablaCatalogo, type ColumnaCatalogo } from '../components/TablaCatalogo'
import { BotonCabecera } from '../components/catalogo/BotonCabecera'
import { ChipEstado } from '../components/catalogo/ChipEstado'
import { ConfirmarAccion } from '../components/catalogo/ConfirmarAccion'
import { EncabezadoPagina } from '../components/catalogo/EncabezadoPagina'
import { PanelFiltros } from '../components/catalogo/PanelFiltros'
import { PanelResumen } from '../components/catalogo/PanelResumen'
import { useConteosCatalogo } from '../hooks/useConteosCatalogo'
import type { Materia } from '../types/catalogo'

const schema = z.object({
  nombre: z.string().min(1, 'El nombre es obligatorio').max(150, 'Máximo 150 caracteres'),
  codigo: z.string().min(1, 'El código es obligatorio').max(50, 'Máximo 50 caracteres'),
  descripcion: z.string().max(255, 'Máximo 255 caracteres'),
})

type FormValues = z.infer<typeof schema>

const ESTADOS = [
  { label: 'Activas', value: 'true' },
  { label: 'Inactivas', value: 'false' },
  { label: 'Todas', value: '' },
] as const

export function MateriasPage() {
  const [materias, setMaterias] = useState<Materia[]>([])
  const [total, setTotal] = useState(0)
  const [pagina, setPagina] = useState(0)
  const [tamano, setTamano] = useState(20)
  const [busqueda, setBusqueda] = useState('')
  const [filtroActivo, setFiltroActivo] = useState('true')
  const [cargando, setCargando] = useState(false)
  const [dialogo, setDialogo] = useState<'crear' | 'editar' | null>(null)
  const [seleccionada, setSeleccionada] = useState<Materia | null>(null)
  const [porConfirmar, setPorConfirmar] = useState<Materia | null>(null)
  const [version, setVersion] = useState(0)

  const { activos, inactivos } = useConteosCatalogo(listarMaterias, version)

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { nombre: '', codigo: '', descripcion: '' },
  })

  const cargar = useCallback(async () => {
    const activo = filtroActivo === '' ? null : filtroActivo === 'true'
    const datos = await listarMaterias({ q: busqueda || null, activo, page: pagina, size: tamano })
    setMaterias(datos.content)
    setTotal(datos.totalElementos)
  }, [busqueda, filtroActivo, pagina, tamano])

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      setCargando(true)
      try {
        if (!cancelado) await cargar()
      } catch {
        if (!cancelado) sileo.error({ title: 'No se pudieron cargar las materias' })
      } finally {
        if (!cancelado) setCargando(false)
      }
    }
    void ejecutar()
    return () => {
      cancelado = true
    }
  }, [cargar])

  const recargar = () => {
    void cargar()
    setVersion((v) => v + 1)
  }

  const abrirCrear = () => {
    form.reset({ nombre: '', codigo: '', descripcion: '' })
    setSeleccionada(null)
    setDialogo('crear')
  }

  const abrirEditar = (materia: Materia) => {
    form.reset({
      nombre: materia.nombre,
      codigo: materia.codigo,
      descripcion: materia.descripcion ?? '',
    })
    setSeleccionada(materia)
    setDialogo('editar')
  }

  const guardar = form.handleSubmit(async (valores) => {
    try {
      if (dialogo === 'editar' && seleccionada) {
        await actualizarMateria(seleccionada.id, { ...valores, activo: seleccionada.activo })
        sileo.success({ title: 'Materia actualizada' })
      } else {
        await crearMateria(valores)
        sileo.success({ title: 'Materia creada' })
      }
      setDialogo(null)
      setPagina(0)
      await cargar()
      setVersion((v) => v + 1)
    } catch {
      sileo.error({ title: 'No se pudo guardar la materia' })
    }
  })

  const confirmarCambioEstado = async () => {
    if (!porConfirmar) return
    try {
      if (porConfirmar.activo) {
        await desactivarMateria(porConfirmar.id)
        sileo.success({ title: 'Materia desactivada' })
      } else {
        await reactivarMateria(porConfirmar.id)
        sileo.success({ title: 'Materia reactivada' })
      }
      setPorConfirmar(null)
      await cargar()
      setVersion((v) => v + 1)
    } catch {
      sileo.error({ title: 'No se pudo cambiar el estado' })
    }
  }

  const columnas: ColumnaCatalogo<Materia>[] = [
    {
      encabezado: 'Nombre',
      render: (m) => <Typography variant="body2" sx={{ fontWeight: 600 }}>{m.nombre}</Typography>,
    },
    { encabezado: 'Código', render: (m) => m.codigo },
    { encabezado: 'Descripción', render: (m) => m.descripcion ?? '—' },
    {
      encabezado: 'Estado',
      render: (m) => <ChipEstado activo={m.activo} etiquetaActivo="Activa" etiquetaInactivo="Inactiva" />,
    },
  ]

  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 1200, mx: 'auto' }}>
      <EncabezadoPagina
        titulo="Materias"
        descripcion="Administra el catálogo de materias de la institución."
        icono={<BookOpen size={26} />}
        accion={
          <BotonCabecera startIcon={<Plus size={18} />} onClick={abrirCrear}>
            Nueva materia
          </BotonCabecera>
        }
      />

      <PanelResumen activos={activos} inactivos={inactivos} />

      <PanelFiltros onRecargar={recargar}>
        <TextField
          label="Buscar materia"
          size="small"
          value={busqueda}
          onChange={(e) => {
            setPagina(0)
            setBusqueda(e.target.value)
          }}
          sx={{ minWidth: { sm: 260 }, flex: { sm: 1 } }}
          slotProps={{
            input: {
              startAdornment: (
                <InputAdornment position="start">
                  <Search size={18} />
                </InputAdornment>
              ),
              endAdornment: busqueda ? (
                <InputAdornment position="end">
                  <IconButton size="small" onClick={() => setBusqueda('')}>
                    <X size={16} />
                  </IconButton>
                </InputAdornment>
              ) : undefined,
            },
          }}
        />
        <FormControl size="small" sx={{ minWidth: 180 }}>
          <InputLabel>Estado</InputLabel>
          <Select
            value={filtroActivo}
            label="Estado"
            onChange={(e) => {
              setPagina(0)
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
        filas={materias}
        cargando={cargando}
        total={total}
        pagina={pagina}
        tamanoPagina={tamano}
        onCambiarPagina={setPagina}
        onCambiarTamano={setTamano}
        claveFila={(m) => m.id}
        mensajeVacio="Aún no hay materias registradas"
        acciones={(m) => (
          <>
            <Tooltip title="Editar">
              <IconButton size="small" onClick={() => abrirEditar(m)} sx={{ color: 'primary.main' }}>
                <Pencil size={17} />
              </IconButton>
            </Tooltip>
            <Tooltip title={m.activo ? 'Desactivar' : 'Reactivar'}>
              <IconButton
                size="small"
                onClick={() => setPorConfirmar(m)}
                sx={{ color: m.activo ? 'warning.main' : 'success.main' }}
              >
                {m.activo ? <PowerOff size={17} /> : <Power size={17} />}
              </IconButton>
            </Tooltip>
          </>
        )}
      />

      <Dialog open={dialogo !== null} onClose={() => setDialogo(null)} maxWidth="sm" fullWidth>
        <form onSubmit={guardar} noValidate>
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
              <BookOpen size={20} />
            </Box>
            {dialogo === 'editar' ? 'Editar materia' : 'Nueva materia'}
          </DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 2 }}>
              <TextField
                label="Nombre"
                fullWidth
                {...form.register('nombre')}
                error={!!form.formState.errors.nombre}
                helperText={form.formState.errors.nombre?.message}
              />
              <TextField
                label="Código"
                fullWidth
                {...form.register('codigo')}
                error={!!form.formState.errors.codigo}
                helperText={form.formState.errors.codigo?.message}
              />
              <TextField
                label="Descripción"
                fullWidth
                multiline
                minRows={2}
                {...form.register('descripcion')}
                error={!!form.formState.errors.descripcion}
                helperText={form.formState.errors.descripcion?.message}
              />
            </Stack>
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 2 }}>
            <Button onClick={() => setDialogo(null)} color="inherit">
              Cancelar
            </Button>
            <Button type="submit" variant="contained" disabled={form.formState.isSubmitting}>
              Guardar
            </Button>
          </DialogActions>
        </form>
      </Dialog>

      <ConfirmarAccion
        abierto={porConfirmar !== null}
        titulo={porConfirmar?.activo ? 'Desactivar materia' : 'Reactivar materia'}
        mensaje={
          porConfirmar?.activo
            ? `¿Deseas desactivar "${porConfirmar?.nombre}"? Ya no estará disponible para nuevas planeaciones.`
            : `¿Deseas reactivar "${porConfirmar?.nombre}"? Volverá a estar disponible.`
        }
        textoConfirmar={porConfirmar?.activo ? 'Desactivar' : 'Reactivar'}
        color={porConfirmar?.activo ? 'warning' : 'success'}
        onConfirmar={confirmarCambioEstado}
        onCerrar={() => setPorConfirmar(null)}
      />
    </Box>
  )
}
