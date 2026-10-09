import { zodResolver } from '@hookform/resolvers/zod'
import axios from 'axios'
import { alpha } from '@mui/material/styles'
import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormHelperText,
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
import dayjs from 'dayjs'
import { CheckCircle2, ClipboardCheck, Clock, Eye, Pencil, Plus, Search, X, XCircle } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { Controller, useForm, useWatch } from 'react-hook-form'
import { sileo } from 'sileo'
import { z } from 'zod'
import { listarMaterias, listarParciales } from '../api/catalogo'
import { actualizarPlaneacion, crearPlaneacion, listarPlaneaciones } from '../api/planeaciones'
import { useAuth } from '../auth/useAuth'
import { SelectorPeriodoParcial } from '../components/SelectorPeriodoParcial'
import { TablaCatalogo, type ColumnaCatalogo } from '../components/TablaCatalogo'
import { BotonCabecera } from '../components/catalogo/BotonCabecera'
import { ChipEstadoPlaneacion } from '../components/catalogo/ChipEstadoPlaneacion'
import { EncabezadoPagina } from '../components/catalogo/EncabezadoPagina'
import { PanelFiltros } from '../components/catalogo/PanelFiltros'
import { TarjetaEstadistica } from '../components/catalogo/TarjetaEstadistica'
import type { Materia } from '../types/catalogo'
import type { EstadoPlaneacion, Planeacion } from '../types/planeaciones'

const schema = z.object({
  materiaId: z.string().min(1, 'Selecciona una materia'),
  periodoId: z.string().min(1, 'Selecciona un periodo'),
  parcialId: z.string().min(1, 'Selecciona un parcial'),
  titulo: z.string().min(1, 'El título es obligatorio').max(200, 'Máximo 200 caracteres'),
  contenido: z.string().min(1, 'El contenido es obligatorio'),
})

type FormValues = z.infer<typeof schema>

const OPCIONES_ESTADO: { label: string; value: EstadoPlaneacion }[] = [
  { label: 'Pendiente', value: 'PENDIENTE' },
  { label: 'Ajustes solicitados', value: 'AJUSTES_SOLICITADOS' },
  { label: 'Aprobada', value: 'APROBADA' },
  { label: 'Rechazada', value: 'RECHAZADA' },
]

function esEditable(estado: EstadoPlaneacion): boolean {
  return estado === 'PENDIENTE' || estado === 'AJUSTES_SOLICITADOS'
}

async function obtenerPeriodoDeParcial(parcialId: string): Promise<string> {
  try {
    const datos = await listarParciales({ activo: true, size: 500 })
    return datos.content.find((p) => p.id === parcialId)?.periodoId ?? ''
  } catch {
    return ''
  }
}

function formatearFecha(fecha: string): string {
  return dayjs(fecha).format('DD/MM/YYYY HH:mm')
}

function mensajeDeError(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const cuerpo = error.response?.data as { message?: string } | undefined
    if (cuerpo?.message) return cuerpo.message
  }
  return 'No se pudo guardar la planeacion'
}

export function PlaneacionesPage() {
  const { usuario } = useAuth()
  const esDocente = usuario?.rol === 'DOCENTE'

  const [planeaciones, setPlaneaciones] = useState<Planeacion[]>([])
  const [materias, setMaterias] = useState<Materia[]>([])
  const [total, setTotal] = useState(0)
  const [pagina, setPagina] = useState(0)
  const [tamano, setTamano] = useState(20)
  const [busqueda, setBusqueda] = useState('')
  const [filtroEstado, setFiltroEstado] = useState('')
  const [filtroMateria, setFiltroMateria] = useState('')
  const [cargando, setCargando] = useState(false)
  const [dialogo, setDialogo] = useState<'crear' | 'editar' | null>(null)
  const [seleccionada, setSeleccionada] = useState<Planeacion | null>(null)
  const [enDetalle, setEnDetalle] = useState<Planeacion | null>(null)
  const [conteos, setConteos] = useState({ enRevision: 0, aprobadas: 0, rechazadas: 0 })
  const [version, setVersion] = useState(0)

  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { materiaId: '', periodoId: '', parcialId: '', titulo: '', contenido: '' },
  })

  const periodoIdActual = useWatch({ control: form.control, name: 'periodoId' })
  const parcialIdActual = useWatch({ control: form.control, name: 'parcialId' })

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      try {
        const datos = await listarMaterias({ activo: true, size: 100, sort: 'nombre' })
        if (!cancelado) setMaterias(datos.content)
      } catch {
        if (!cancelado) setMaterias([])
      }
    }
    void ejecutar()
    return () => {
      cancelado = true
    }
  }, [])

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      try {
        const [pendiente, ajustes, aprobadas, rechazadas] = await Promise.all([
          listarPlaneaciones({ estado: 'PENDIENTE', page: 0, size: 1 }),
          listarPlaneaciones({ estado: 'AJUSTES_SOLICITADOS', page: 0, size: 1 }),
          listarPlaneaciones({ estado: 'APROBADA', page: 0, size: 1 }),
          listarPlaneaciones({ estado: 'RECHAZADA', page: 0, size: 1 }),
        ])
        if (!cancelado) {
          setConteos({
            enRevision: pendiente.totalElementos + ajustes.totalElementos,
            aprobadas: aprobadas.totalElementos,
            rechazadas: rechazadas.totalElementos,
          })
        }
      } catch {
        if (!cancelado) {
          setConteos({ enRevision: 0, aprobadas: 0, rechazadas: 0 })
        }
      }
    }
    void ejecutar()
    return () => {
      cancelado = true
    }
  }, [version])

  const cargar = useCallback(async () => {
    const datos = await listarPlaneaciones({
      q: busqueda || null,
      estado: (filtroEstado || null) as EstadoPlaneacion | null,
      materiaId: filtroMateria || null,
      page: pagina,
      size: tamano,
    })
    setPlaneaciones(datos.content)
    setTotal(datos.totalElementos)
  }, [busqueda, filtroEstado, filtroMateria, pagina, tamano])

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      setCargando(true)
      try {
        if (!cancelado) await cargar()
      } catch {
        if (!cancelado) sileo.error({ title: 'No se pudieron cargar las planeaciones' })
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
    form.reset({ materiaId: '', periodoId: '', parcialId: '', titulo: '', contenido: '' })
    setSeleccionada(null)
    setDialogo('crear')
  }

  const abrirEditar = async (planeacion: Planeacion) => {
    const periodoId = await obtenerPeriodoDeParcial(planeacion.parcialId)
    form.reset({
      materiaId: planeacion.materiaId,
      periodoId,
      parcialId: planeacion.parcialId,
      titulo: planeacion.titulo,
      contenido: planeacion.contenido,
    })
    setSeleccionada(planeacion)
    setDialogo('editar')
  }

  const editarDesdeDetalle = () => {
    if (!enDetalle) return
    setEnDetalle(null)
    setDialogo('editar')
    void abrirEditar(enDetalle)
  }

  const guardar = form.handleSubmit(async (valores) => {
    try {
      const payload = {
        materiaId: valores.materiaId,
        parcialId: valores.parcialId,
        titulo: valores.titulo,
        contenido: valores.contenido,
      }
      if (dialogo === 'editar' && seleccionada) {
        await actualizarPlaneacion(seleccionada.id, payload)
        sileo.success({ title: 'Planeacion actualizada' })
      } else {
        await crearPlaneacion(payload)
        sileo.success({ title: 'Planeacion registrada' })
      }
      setDialogo(null)
      setSeleccionada(null)
      setPagina(0)
      await cargar()
      setVersion((v) => v + 1)
    } catch (error) {
      sileo.error({ title: mensajeDeError(error) })
    }
  })

  const columnas: ColumnaCatalogo<Planeacion>[] = [
    {
      encabezado: 'Titulo',
      render: (p) => (
        <Box sx={{ minWidth: 0 }}>
          <Typography variant="body2" sx={{ fontWeight: 600 }}>
            {p.titulo}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {p.materiaCodigo}
          </Typography>
        </Box>
      ),
    },
    { encabezado: 'Materia', render: (p) => p.materiaNombre },
    { encabezado: 'Parcial', render: (p) => p.parcialNombre },
    { encabezado: 'Estado', render: (p) => <ChipEstadoPlaneacion estado={p.estado} /> },
    { encabezado: 'Creada', render: (p) => formatearFecha(p.createdAt) },
  ]

  const errorSelector =
    form.formState.errors.periodoId?.message ?? form.formState.errors.parcialId?.message

  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 1200, mx: 'auto' }}>
      <EncabezadoPagina
        titulo={esDocente ? 'Mis planeaciones' : 'Planeaciones'}
        descripcion={
          esDocente
            ? 'Registra y da seguimiento a tus planeaciones didacticas.'
            : 'Consulta el estado de las planeaciones didacticas de la institucion.'
        }
        icono={<ClipboardCheck size={26} />}
        accion={
          esDocente ? (
            <BotonCabecera startIcon={<Plus size={18} />} onClick={abrirCrear} disabled={materias.length === 0}>
              Nueva planeacion
            </BotonCabecera>
          ) : undefined
        }
      />

      {esDocente && materias.length === 0 && (
        <Alert severity="info" sx={{ mb: 3 }}>
          Aun no hay materias activas. Un coordinador debe registrar materias y periodos antes de que puedas crear
          planeaciones.
        </Alert>
      )}

      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: { xs: '1fr', sm: 'repeat(3, 1fr)' },
          mb: 3,
        }}
      >
        <TarjetaEstadistica etiqueta="En revision" valor={conteos.enRevision} icono={<Clock size={22} />} variedad="primario" />
        <TarjetaEstadistica etiqueta="Aprobadas" valor={conteos.aprobadas} icono={<CheckCircle2 size={22} />} variedad="exito" />
        <TarjetaEstadistica etiqueta="Rechazadas" valor={conteos.rechazadas} icono={<XCircle size={22} />} variedad="neutro" />
      </Box>

      <PanelFiltros onRecargar={recargar}>
        <TextField
          label="Buscar por titulo"
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
        <FormControl size="small" sx={{ minWidth: 190 }}>
          <InputLabel>Estado</InputLabel>
          <Select
            value={filtroEstado}
            label="Estado"
            onChange={(e) => {
              setPagina(0)
              setFiltroEstado(e.target.value)
            }}
          >
            <MenuItem value="">Todos</MenuItem>
            {OPCIONES_ESTADO.map((e) => (
              <MenuItem key={e.value} value={e.value}>
                {e.label}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
        <FormControl size="small" sx={{ minWidth: 200 }}>
          <InputLabel>Materia</InputLabel>
          <Select
            value={filtroMateria}
            label="Materia"
            onChange={(e) => {
              setPagina(0)
              setFiltroMateria(e.target.value)
            }}
          >
            <MenuItem value="">Todas</MenuItem>
            {materias.map((m) => (
              <MenuItem key={m.id} value={m.id}>
                {m.codigo} · {m.nombre}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </PanelFiltros>

      <TablaCatalogo
        columnas={columnas}
        filas={planeaciones}
        cargando={cargando}
        total={total}
        pagina={pagina}
        tamanoPagina={tamano}
        onCambiarPagina={setPagina}
        onCambiarTamano={setTamano}
        claveFila={(p) => p.id}
        mensajeVacio={esDocente ? 'Aun no tienes planeaciones registradas' : 'Aun no hay planeaciones registradas'}
        detalleVacio={esDocente ? 'Crea tu primera planeacion para comenzar a dar seguimiento.' : undefined}
        acciones={(p) => (
          <>
            <Tooltip title="Ver detalle">
              <IconButton size="small" onClick={() => setEnDetalle(p)}>
                <Eye size={17} />
              </IconButton>
            </Tooltip>
            {esDocente && esEditable(p.estado) && (
              <Tooltip title="Editar">
                <IconButton size="small" onClick={() => void abrirEditar(p)} sx={{ color: 'primary.main' }}>
                  <Pencil size={17} />
                </IconButton>
              </Tooltip>
            )}
          </>
        )}
      />

      <Dialog
        open={dialogo !== null}
        onClose={() => {
          setDialogo(null)
          setSeleccionada(null)
        }}
        maxWidth="sm"
        fullWidth
      >
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
              <ClipboardCheck size={20} />
            </Box>
            {dialogo === 'editar' ? 'Editar planeacion' : 'Nueva planeacion'}
          </DialogTitle>
          <DialogContent>
            <Stack spacing={2} sx={{ mt: 2 }}>
              <Controller
                name="materiaId"
                control={form.control}
                render={({ field }) => (
                  <FormControl fullWidth error={!!form.formState.errors.materiaId}>
                    <InputLabel>Materia</InputLabel>
                    <Select {...field} label="Materia">
                      {materias.map((m) => (
                        <MenuItem key={m.id} value={m.id}>
                          {m.codigo} · {m.nombre}
                        </MenuItem>
                      ))}
                    </Select>
                    {form.formState.errors.materiaId && (
                      <FormHelperText error>{form.formState.errors.materiaId.message}</FormHelperText>
                    )}
                  </FormControl>
                )}
              />
              <Box>
                <SelectorPeriodoParcial
                  periodoId={periodoIdActual}
                  parcialId={parcialIdActual}
                  onPeriodoChange={(id) => {
                    form.setValue('periodoId', id, { shouldValidate: true })
                    form.setValue('parcialId', '', { shouldValidate: true })
                  }}
                  onParcialChange={(id) => form.setValue('parcialId', id, { shouldValidate: true })}
                />
                {errorSelector && <FormHelperText error>{errorSelector}</FormHelperText>}
              </Box>
              <TextField
                label="Titulo"
                fullWidth
                {...form.register('titulo')}
                error={!!form.formState.errors.titulo}
                helperText={form.formState.errors.titulo?.message}
              />
              <TextField
                label="Contenido"
                fullWidth
                multiline
                minRows={6}
                {...form.register('contenido')}
                error={!!form.formState.errors.contenido}
                helperText={form.formState.errors.contenido?.message}
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

      <Dialog open={enDetalle !== null} onClose={() => setEnDetalle(null)} maxWidth="sm" fullWidth>
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
            <Eye size={20} />
          </Box>
          Detalle de la planeacion
        </DialogTitle>
        <DialogContent>
          {enDetalle && (
            <Stack spacing={2.5} sx={{ mt: 2 }}>
              <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between', gap: 1, flexWrap: 'wrap' }}>
                <ChipEstadoPlaneacion estado={enDetalle.estado} />
                <Typography variant="caption" color="text.secondary">
                  Creada {formatearFecha(enDetalle.createdAt)}
                </Typography>
              </Stack>
              <DetalleCampo etiqueta="Materia" valor={`${enDetalle.materiaCodigo} · ${enDetalle.materiaNombre}`} />
              <DetalleCampo etiqueta="Parcial" valor={enDetalle.parcialNombre} />
              <DetalleCampo etiqueta="Titulo" valor={enDetalle.titulo} />
              <DetalleCampo etiqueta="Contenido" valor={enDetalle.contenido} />
            </Stack>
          )}
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setEnDetalle(null)} color="inherit">
            Cerrar
          </Button>
          {enDetalle && esEditable(enDetalle.estado) && esDocente && (
            <Button onClick={editarDesdeDetalle} variant="outlined" startIcon={<Pencil size={16} />}>
              Editar
            </Button>
          )}
        </DialogActions>
      </Dialog>
    </Box>
  )
}

function DetalleCampo({ etiqueta, valor }: { etiqueta: string; valor: string }) {
  return (
    <Box>
      <Typography
        variant="caption"
        color="text.secondary"
        sx={{ fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em' }}
      >
        {etiqueta}
      </Typography>
      <Typography variant="body2" sx={{ mt: 0.25, whiteSpace: 'pre-wrap' }}>
        {valor}
      </Typography>
    </Box>
  )
}