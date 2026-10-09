import { zodResolver } from '@hookform/resolvers/zod'
import {
  Alert,
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
import { ListChecks, Pencil, Plus, Power, PowerOff, Search, X } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { sileo } from 'sileo'
import { z } from 'zod'
import {
  actualizarParcial,
  crearParcial,
  desactivarParcial,
  listarParciales,
  listarPeriodos,
  reactivarParcial,
} from '../api/catalogo'
import { TablaCatalogo, type ColumnaCatalogo } from '../components/TablaCatalogo'
import { BotonCabecera } from '../components/catalogo/BotonCabecera'
import { ChipEstado } from '../components/catalogo/ChipEstado'
import { ConfirmarAccion } from '../components/catalogo/ConfirmarAccion'
import { EncabezadoPagina } from '../components/catalogo/EncabezadoPagina'
import { PanelFiltros } from '../components/catalogo/PanelFiltros'
import { PanelResumen } from '../components/catalogo/PanelResumen'
import { useConteosCatalogo } from '../hooks/useConteosCatalogo'
import type { Parcial, Periodo } from '../types/catalogo'

const schema = z
  .object({
    periodoId: z.string().min(1, 'Selecciona un periodo'),
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

export function ParcialesPage() {
  const [parciales, setParciales] = useState<Parcial[]>([])
  const [periodos, setPeriodos] = useState<Periodo[]>([])
  const [total, setTotal] = useState(0)
  const [pagina, setPagina] = useState(0)
  const [tamano, setTamano] = useState(20)
  const [busqueda, setBusqueda] = useState('')
  const [filtroActivo, setFiltroActivo] = useState('true')
  const [filtroPeriodo, setFiltroPeriodo] = useState('')
  const [cargando, setCargando] = useState(false)
  const [dialogo, setDialogo] = useState<'crear' | 'editar' | null>(null)
  const [seleccionado, setSeleccionado] = useState<Parcial | null>(null)
  const [porConfirmar, setPorConfirmar] = useState<Parcial | null>(null)
  const [version, setVersion] = useState(0)

  const { activos, inactivos } = useConteosCatalogo(listarParciales, version)

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { periodoId: '', nombre: '', fechaInicio: '', fechaFin: '' },
  })

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      try {
        const datos = await listarPeriodos({ activo: true, size: 100, sort: 'fechaInicio,desc' })
        if (!cancelado) setPeriodos(datos.content)
      } catch {
        if (!cancelado) sileo.error({ title: 'No se pudieron cargar los periodos' })
      }
    }
    void ejecutar()
    return () => {
      cancelado = true
    }
  }, [])

  const cargar = useCallback(async () => {
    const activo = filtroActivo === '' ? null : filtroActivo === 'true'
    const datos = await listarParciales({
      periodoId: filtroPeriodo || null,
      q: busqueda || null,
      activo,
      page: pagina,
      size: tamano,
    })
    setParciales(datos.content)
    setTotal(datos.totalElementos)
  }, [filtroPeriodo, busqueda, filtroActivo, pagina, tamano])

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      setCargando(true)
      try {
        if (!cancelado) await cargar()
      } catch {
        if (!cancelado) sileo.error({ title: 'No se pudieron cargar los parciales' })
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
    form.reset({
      periodoId: filtroPeriodo || periodos[0]?.id || '',
      nombre: '',
      fechaInicio: '',
      fechaFin: '',
    })
    setSeleccionado(null)
    setDialogo('crear')
  }

  const abrirEditar = (parcial: Parcial) => {
    form.reset({
      periodoId: parcial.periodoId,
      nombre: parcial.nombre,
      fechaInicio: parcial.fechaInicio,
      fechaFin: parcial.fechaFin,
    })
    setSeleccionado(parcial)
    setDialogo('editar')
  }

  const guardar = form.handleSubmit(async (valores) => {
    try {
      if (dialogo === 'editar' && seleccionado) {
        await actualizarParcial(seleccionado.id, { ...valores, activo: seleccionado.activo })
        sileo.success({ title: 'Parcial actualizado' })
      } else {
        await crearParcial(valores)
        sileo.success({ title: 'Parcial creado' })
      }
      setDialogo(null)
      setPagina(0)
      await cargar()
      setVersion((v) => v + 1)
    } catch {
      sileo.error({ title: 'No se pudo guardar el parcial' })
    }
  })

  const confirmarCambioEstado = async () => {
    if (!porConfirmar) return
    try {
      if (porConfirmar.activo) {
        await desactivarParcial(porConfirmar.id)
        sileo.success({ title: 'Parcial desactivado' })
      } else {
        await reactivarParcial(porConfirmar.id)
        sileo.success({ title: 'Parcial reactivado' })
      }
      setPorConfirmar(null)
      await cargar()
      setVersion((v) => v + 1)
    } catch {
      sileo.error({ title: 'No se pudo cambiar el estado' })
    }
  }

  const columnas: ColumnaCatalogo<Parcial>[] = [
    {
      encabezado: 'Nombre',
      render: (p) => <Typography variant="body2" sx={{ fontWeight: 600 }}>{p.nombre}</Typography>,
    },
    { encabezado: 'Periodo', render: (p) => p.periodoNombre ?? '—' },
    { encabezado: 'Inicio', render: (p) => formatearFecha(p.fechaInicio) },
    { encabezado: 'Fin', render: (p) => formatearFecha(p.fechaFin) },
    { encabezado: 'Estado', render: (p) => <ChipEstado activo={p.activo} /> },
  ]

  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 1200, mx: 'auto' }}>
      <EncabezadoPagina
        titulo="Parciales"
        descripcion="Organiza los parciales de cada periodo académico."
        icono={<ListChecks size={26} color="#fff" />}
        accion={
          <BotonCabecera
            startIcon={<Plus size={18} />}
            onClick={abrirCrear}
            disabled={periodos.length === 0}
          >
            Nuevo parcial
          </BotonCabecera>
        }
      />

      {periodos.length === 0 && (
        <Alert severity="info" sx={{ mb: 3 }}>
          Crea al menos un periodo activo para poder registrar parciales.
        </Alert>
      )}

      <PanelResumen activos={activos} inactivos={inactivos} />

      <PanelFiltros onRecargar={recargar}>
        <TextField
          label="Buscar parcial"
          size="small"
          value={busqueda}
          onChange={(e) => {
            setPagina(0)
            setBusqueda(e.target.value)
          }}
          sx={{ minWidth: { sm: 220 }, flex: { sm: 1 } }}
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
        <FormControl size="small" sx={{ minWidth: 200 }}>
          <InputLabel>Periodo</InputLabel>
          <Select
            value={filtroPeriodo}
            label="Periodo"
            onChange={(e) => {
              setPagina(0)
              setFiltroPeriodo(e.target.value)
            }}
          >
            <MenuItem value="">Todos</MenuItem>
            {periodos.map((p) => (
              <MenuItem key={p.id} value={p.id}>
                {p.nombre}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        <FormControl size="small" sx={{ minWidth: 160 }}>
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
        filas={parciales}
        cargando={cargando}
        total={total}
        pagina={pagina}
        tamanoPagina={tamano}
        onCambiarPagina={setPagina}
        onCambiarTamano={setTamano}
        claveFila={(p) => p.id}
        mensajeVacio="Aún no hay parciales registrados"
        acciones={(p) => (
          <>
            <Tooltip title="Editar">
              <IconButton size="small" onClick={() => abrirEditar(p)} sx={{ color: '#4f46e5' }}>
                <Pencil size={17} />
              </IconButton>
            </Tooltip>
            <Tooltip title={p.activo ? 'Desactivar' : 'Reactivar'}>
              <IconButton
                size="small"
                onClick={() => setPorConfirmar(p)}
                sx={{ color: p.activo ? '#d97706' : '#059669' }}
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
                color: '#4f46e5',
                background: '#eef2ff',
              }}
            >
              <ListChecks size={20} />
            </Box>
            {dialogo === 'editar' ? 'Editar parcial' : 'Nuevo parcial'}
          </DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 2 }}>
              <Controller
                name="periodoId"
                control={form.control}
                render={({ field }) => (
                  <FormControl fullWidth error={!!form.formState.errors.periodoId}>
                    <InputLabel>Periodo</InputLabel>
                    <Select {...field} label="Periodo">
                      {periodos.map((p) => (
                        <MenuItem key={p.id} value={p.id}>
                          {p.nombre}
                        </MenuItem>
                      ))}
                    </Select>
                  </FormControl>
                )}
              />
              <TextField
                label="Nombre"
                fullWidth
                {...form.register('nombre')}
                error={!!form.formState.errors.nombre}
                helperText={form.formState.errors.nombre?.message}
              />
              <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2}>
                <TextField
                  label="Fecha de inicio"
                  type="date"
                  fullWidth
                  slotProps={{ inputLabel: { shrink: true } }}
                  {...form.register('fechaInicio')}
                  error={!!form.formState.errors.fechaInicio}
                  helperText={form.formState.errors.fechaInicio?.message}
                />
                <TextField
                  label="Fecha de fin"
                  type="date"
                  fullWidth
                  slotProps={{ inputLabel: { shrink: true } }}
                  {...form.register('fechaFin')}
                  error={!!form.formState.errors.fechaFin}
                  helperText={form.formState.errors.fechaFin?.message}
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
        titulo={porConfirmar?.activo ? 'Desactivar parcial' : 'Reactivar parcial'}
        mensaje={
          porConfirmar?.activo
            ? `¿Deseas desactivar "${porConfirmar?.nombre}"? Dejará de estar disponible para la planeación.`
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
