import { zodResolver } from '@hookform/resolvers/zod'
import { alpha } from '@mui/material/styles'
import { DatePicker } from '@mui/x-date-pickers/DatePicker'
import dayjs from 'dayjs'
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
import { CalendarRange, Pencil, Plus, Power, PowerOff, Search, X } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { sileo } from 'sileo'
import { z } from 'zod'
import {
  actualizarPeriodo,
  crearPeriodo,
  desactivarPeriodo,
  listarPeriodos,
  reactivarPeriodo,
} from '../api/catalogo'
import { TablaCatalogo, type ColumnaCatalogo } from '../components/TablaCatalogo'
import { BotonCabecera } from '../components/catalogo/BotonCabecera'
import { ChipEstado } from '../components/catalogo/ChipEstado'
import { ConfirmarAccion } from '../components/catalogo/ConfirmarAccion'
import { EncabezadoPagina } from '../components/catalogo/EncabezadoPagina'
import { PanelFiltros } from '../components/catalogo/PanelFiltros'
import { PanelResumen } from '../components/catalogo/PanelResumen'
import { useConteosCatalogo } from '../hooks/useConteosCatalogo'
import type { Periodo } from '../types/catalogo'

const schema = z
  .object({
    nombre: z.string().min(1, 'El nombre es obligatorio').max(100, 'Máximo 100 caracteres'),
    fechaInicio: z.string().min(1, 'La fecha de inicio es obligatoria'),
    fechaFin: z.string().min(1, 'La fecha de fin es obligatoria'),
  })
  .refine((v) => !v.fechaInicio || !v.fechaFin || v.fechaFin >= v.fechaInicio, {
    message: 'La fecha de fin no puede ser anterior a la de inicio',
    path: ['fechaFin'],
  })

type FormValues = z.infer<typeof schema>

const ESTADOS = [
  { label: 'Activos', value: 'true' },
  { label: 'Inactivos', value: 'false' },
  { label: 'Todos', value: '' },
] as const

function formatearFecha(fecha: string) {
  const [anio, mes, dia] = fecha.split('-')
  return `${dia}/${mes}/${anio}`
}

export function PeriodosPage() {
  const [periodos, setPeriodos] = useState<Periodo[]>([])
  const [total, setTotal] = useState(0)
  const [pagina, setPagina] = useState(0)
  const [tamano, setTamano] = useState(20)
  const [busqueda, setBusqueda] = useState('')
  const [filtroActivo, setFiltroActivo] = useState('true')
  const [cargando, setCargando] = useState(false)
  const [dialogo, setDialogo] = useState<'crear' | 'editar' | null>(null)
  const [seleccionado, setSeleccionado] = useState<Periodo | null>(null)
  const [porConfirmar, setPorConfirmar] = useState<Periodo | null>(null)
  const [version, setVersion] = useState(0)

  const { activos, inactivos } = useConteosCatalogo(listarPeriodos, version)

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { nombre: '', fechaInicio: '', fechaFin: '' },
  })

  const cargar = useCallback(async () => {
    const activo = filtroActivo === '' ? null : filtroActivo === 'true'
    const datos = await listarPeriodos({ q: busqueda || null, activo, page: pagina, size: tamano })
    setPeriodos(datos.content)
    setTotal(datos.totalElementos)
  }, [busqueda, filtroActivo, pagina, tamano])

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      setCargando(true)
      try {
        if (!cancelado) await cargar()
      } catch {
        if (!cancelado) sileo.error({ title: 'No se pudieron cargar los periodos' })
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
    form.reset({ nombre: '', fechaInicio: '', fechaFin: '' })
    setSeleccionado(null)
    setDialogo('crear')
  }

  const abrirEditar = (periodo: Periodo) => {
    form.reset({
      nombre: periodo.nombre,
      fechaInicio: periodo.fechaInicio,
      fechaFin: periodo.fechaFin,
    })
    setSeleccionado(periodo)
    setDialogo('editar')
  }

  const guardar = form.handleSubmit(async (valores) => {
    try {
      if (dialogo === 'editar' && seleccionado) {
        await actualizarPeriodo(seleccionado.id, { ...valores, activo: seleccionado.activo })
        sileo.success({ title: 'Periodo actualizado' })
      } else {
        await crearPeriodo(valores)
        sileo.success({ title: 'Periodo creado' })
      }
      setDialogo(null)
      setPagina(0)
      await cargar()
      setVersion((v) => v + 1)
    } catch {
      sileo.error({ title: 'No se pudo guardar el periodo' })
    }
  })

  const confirmarCambioEstado = async () => {
    if (!porConfirmar) return
    try {
      if (porConfirmar.activo) {
        await desactivarPeriodo(porConfirmar.id)
        sileo.success({ title: 'Periodo desactivado' })
      } else {
        await reactivarPeriodo(porConfirmar.id)
        sileo.success({ title: 'Periodo reactivado' })
      }
      setPorConfirmar(null)
      await cargar()
      setVersion((v) => v + 1)
    } catch {
      sileo.error({ title: 'No se pudo cambiar el estado' })
    }
  }

  const columnas: ColumnaCatalogo<Periodo>[] = [
    {
      encabezado: 'Nombre',
      render: (p) => <Typography variant="body2" sx={{ fontWeight: 600 }}>{p.nombre}</Typography>,
    },
    { encabezado: 'Inicio', render: (p) => formatearFecha(p.fechaInicio) },
    { encabezado: 'Fin', render: (p) => formatearFecha(p.fechaFin) },
    { encabezado: 'Estado', render: (p) => <ChipEstado activo={p.activo} /> },
  ]

  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 1200, mx: 'auto' }}>
      <EncabezadoPagina
        titulo="Periodos"
        descripcion="Define los periodos académicos vigentes."
        icono={<CalendarRange size={26} />}
        accion={
          <BotonCabecera startIcon={<Plus size={18} />} onClick={abrirCrear}>
            Nuevo periodo
          </BotonCabecera>
        }
      />

      <PanelResumen activos={activos} inactivos={inactivos} />

      <PanelFiltros onRecargar={recargar}>
        <TextField
          label="Buscar periodo"
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
        filas={periodos}
        cargando={cargando}
        total={total}
        pagina={pagina}
        tamanoPagina={tamano}
        onCambiarPagina={setPagina}
        onCambiarTamano={setTamano}
        claveFila={(p) => p.id}
        mensajeVacio="Aún no hay periodos registrados"
        acciones={(p) => (
          <>
            <Tooltip title="Editar">
              <IconButton size="small" onClick={() => abrirEditar(p)} sx={{ color: 'primary.main' }}>
                <Pencil size={17} />
              </IconButton>
            </Tooltip>
            <Tooltip title={p.activo ? 'Desactivar' : 'Reactivar'}>
              <IconButton
                size="small"
                onClick={() => setPorConfirmar(p)}
                sx={{ color: p.activo ? 'warning.main' : 'success.main' }}
              >
                {p.activo ? <PowerOff size={17} /> : <Power size={17} />}
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
              <CalendarRange size={20} />
            </Box>
            {dialogo === 'editar' ? 'Editar periodo' : 'Nuevo periodo'}
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
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                <Controller
                  name="fechaInicio"
                  control={form.control}
                  render={({ field }) => (
                    <DatePicker
                      label="Fecha de inicio"
                      value={field.value ? dayjs(field.value) : null}
                      onChange={(fecha) => field.onChange(fecha ? fecha.format('YYYY-MM-DD') : '')}
                      slotProps={{
                        textField: {
                          fullWidth: true,
                          error: !!form.formState.errors.fechaInicio,
                          helperText: form.formState.errors.fechaInicio?.message,
                        },
                      }}
                    />
                  )}
                />
                <Controller
                  name="fechaFin"
                  control={form.control}
                  render={({ field }) => (
                    <DatePicker
                      label="Fecha de fin"
                      value={field.value ? dayjs(field.value) : null}
                      onChange={(fecha) => field.onChange(fecha ? fecha.format('YYYY-MM-DD') : '')}
                      slotProps={{
                        textField: {
                          fullWidth: true,
                          error: !!form.formState.errors.fechaFin,
                          helperText: form.formState.errors.fechaFin?.message,
                        },
                      }}
                    />
                  )}
                />
              </Stack>
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
        titulo={porConfirmar?.activo ? 'Desactivar periodo' : 'Reactivar periodo'}
        mensaje={
          porConfirmar?.activo
            ? `¿Deseas desactivar "${porConfirmar?.nombre}"? Los parciales asociados dejarán de estar disponibles.`
            : `¿Deseas reactivar "${porConfirmar?.nombre}"? Volverá a estar disponible en la planeación.`
        }
        textoConfirmar={porConfirmar?.activo ? 'Desactivar' : 'Reactivar'}
        color={porConfirmar?.activo ? 'warning' : 'success'}
        onConfirmar={confirmarCambioEstado}
        onCerrar={() => setPorConfirmar(null)}
      />
    </Box>
  )
}
