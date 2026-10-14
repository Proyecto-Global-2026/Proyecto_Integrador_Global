import axios from 'axios'
import { alpha } from '@mui/material/styles'
import {
  Box,
  Button,
  Chip,
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
import dayjs from 'dayjs'
import { Pencil, Plus, Search, Trash2, TrendingUp, X } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { sileo } from 'sileo'
import {
  actualizarAvance,
  eliminarAvance,
  listarAvances,
  registrarAvance,
} from '../api/avances'
import { listarMaterias, listarParciales } from '../api/catalogo'
import { listarPlaneaciones } from '../api/planeaciones'
import { TablaCatalogo, type ColumnaCatalogo } from '../components/TablaCatalogo'
import { ChipEstadoAvance } from '../components/catalogo/ChipEstadoAvance'
import { ConfirmarAccion } from '../components/catalogo/ConfirmarAccion'
import { EncabezadoPagina } from '../components/catalogo/EncabezadoPagina'
import { PanelFiltros } from '../components/catalogo/PanelFiltros'
import { ESTADOS_AVANCE, ETIQUETAS_AVANCE, type Avance, type EstadoAvance } from '../types/avances'
import type { Materia, Parcial } from '../types/catalogo'
import type { Planeacion } from '../types/planeaciones'

function formatearFecha(fecha: string): string {
  return dayjs(fecha).format('DD/MM/YYYY HH:mm')
}

function mensajeDeError(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const cuerpo = error.response?.data as { message?: string } | undefined
    if (cuerpo?.message) return cuerpo.message
  }
  return 'No se pudo guardar el avance'
}

export function AvancesPage() {
  const [planeaciones, setPlaneaciones] = useState<Planeacion[]>([])
  const [avances, setAvances] = useState<Map<string, Avance>>(new Map())
  const [materias, setMaterias] = useState<Materia[]>([])
  const [parciales, setParciales] = useState<Parcial[]>([])
  const [total, setTotal] = useState(0)
  const [pagina, setPagina] = useState(0)
  const [tamano, setTamano] = useState(20)
  const [busqueda, setBusqueda] = useState('')
  const [filtroMateria, setFiltroMateria] = useState('')
  const [filtroParcial, setFiltroParcial] = useState('')
  const [cargando, setCargando] = useState(false)

  const [enFormulario, setEnFormulario] = useState<Planeacion | null>(null)
  const [avanceEditando, setAvanceEditando] = useState<Avance | null>(null)
  const [estado, setEstado] = useState<EstadoAvance>('CUMPLIDO')
  const [observaciones, setObservaciones] = useState('')
  const [guardando, setGuardando] = useState(false)
  const [porEliminar, setPorEliminar] = useState<Planeacion | null>(null)

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      try {
        const [listaMaterias, listaParciales] = await Promise.all([
          listarMaterias({ size: 100, sort: 'nombre' }),
          listarParciales({ size: 100, sort: 'fechaInicio,desc' }),
        ])
        if (!cancelado) {
          setMaterias(listaMaterias.content)
          setParciales(listaParciales.content)
        }
      } catch {
        if (!cancelado) sileo.error({ title: 'No se pudieron cargar los filtros' })
      }
    }
    void ejecutar()
    return () => {
      cancelado = true
    }
  }, [])

  const cargar = useCallback(async () => {
    const datos = await listarPlaneaciones({
      q: busqueda || null,
      materiaId: filtroMateria || null,
      parcialId: filtroParcial || null,
      page: pagina,
      size: tamano,
    })
    const resultados = await Promise.all(
      datos.content.map(async (p) => {
        const lista = await listarAvances(p.id)
        return [p.id, lista[0] ?? null] as const
      }),
    )
    const mapa = new Map<string, Avance>()
    resultados.forEach(([id, avance]) => {
      if (avance) mapa.set(id, avance)
    })
    setPlaneaciones(datos.content)
    setAvances(mapa)
    setTotal(datos.totalElementos)
  }, [busqueda, filtroMateria, filtroParcial, pagina, tamano])

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      setCargando(true)
      try {
        if (!cancelado) await cargar()
      } catch {
        if (!cancelado) sileo.error({ title: 'No se pudieron cargar los avances' })
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
    if (pagina === 0) {
      void cargar()
    } else {
      setPagina(0)
    }
  }

  const abrirFormulario = (planeacion: Planeacion) => {
    const avance = avances.get(planeacion.id) ?? null
    setAvanceEditando(avance)
    setEstado(avance?.estado ?? 'CUMPLIDO')
    setObservaciones(avance?.observaciones ?? '')
    setEnFormulario(planeacion)
  }

  const guardar = async () => {
    if (!enFormulario) return
    setGuardando(true)
    try {
      const payload = { estado, observaciones: observaciones.trim() || undefined }
      if (avanceEditando) {
        await actualizarAvance(enFormulario.id, avanceEditando.id, payload)
      } else {
        await registrarAvance(enFormulario.id, payload)
      }
      sileo.success({ title: avanceEditando ? 'Avance actualizado' : 'Avance registrado' })
      setEnFormulario(null)
      recargar()
    } catch (error) {
      sileo.error({ title: mensajeDeError(error) })
    } finally {
      setGuardando(false)
    }
  }

  const confirmarEliminar = async () => {
    if (!porEliminar) return
    const avance = avances.get(porEliminar.id)
    if (!avance) {
      setPorEliminar(null)
      return
    }
    try {
      await eliminarAvance(porEliminar.id, avance.id)
      sileo.success({ title: 'Avance eliminado' })
      setPorEliminar(null)
      recargar()
    } catch {
      sileo.error({ title: 'No se pudo eliminar el avance' })
      setPorEliminar(null)
    }
  }

  const columnas: ColumnaCatalogo<Planeacion>[] = [
    {
      encabezado: 'Materia',
      render: (p) => (
        <Box sx={{ minWidth: 0 }}>
          <Typography variant="body2" sx={{ fontWeight: 600 }}>
            {p.materiaNombre}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {p.materiaCodigo}
          </Typography>
        </Box>
      ),
    },
    { encabezado: 'Parcial', render: (p) => p.parcialNombre },
    { encabezado: 'Planeacion', render: (p) => p.titulo },
    {
      encabezado: 'Estado',
      render: (p) => {
        const avance = avances.get(p.id)
        return avance ? (
          <ChipEstadoAvance estado={avance.estado} />
        ) : (
          <Chip size="small" variant="outlined" label="Sin registrar" />
        )
      },
    },
    { encabezado: 'Observaciones', render: (p) => avances.get(p.id)?.observaciones ?? '—' },
    {
      encabezado: 'Actualizado',
      render: (p) => {
        const avance = avances.get(p.id)
        return avance ? formatearFecha(avance.updatedAt) : '—'
      },
    },
  ]

  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 1200, mx: 'auto' }}>
      <EncabezadoPagina
        titulo="Avances por parcial"
        descripcion="Registra y consulta el avance logrado en el parcial de cada planeacion."
        icono={<TrendingUp size={26} />}
      />

      <PanelFiltros onRecargar={recargar}>
        <TextField
          label="Buscar por titulo"
          size="small"
          value={busqueda}
          onChange={(e) => {
            setPagina(0)
            setBusqueda(e.target.value)
          }}
          sx={{ minWidth: { sm: 200 }, flex: { sm: 1 } }}
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
        <FormControl size="small" sx={{ minWidth: 180 }}>
          <InputLabel>Parcial</InputLabel>
          <Select
            value={filtroParcial}
            label="Parcial"
            onChange={(e) => {
              setPagina(0)
              setFiltroParcial(e.target.value)
            }}
          >
            <MenuItem value="">Todos</MenuItem>
            {parciales.map((p) => (
              <MenuItem key={p.id} value={p.id}>
                {p.nombre}
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
        mensajeVacio="No hay planeaciones para consultar avances"
        detalleVacio="Registra una planeacion o ajusta los filtros de materia y parcial."
        acciones={(p) => {
          const avance = avances.get(p.id)
          return (
            <>
              <Tooltip title={avance ? 'Editar avance' : 'Registrar avance'}>
                <IconButton size="small" onClick={() => abrirFormulario(p)} sx={{ color: 'primary.main' }}>
                  {avance ? <Pencil size={17} /> : <Plus size={17} />}
                </IconButton>
              </Tooltip>
              {avance && (
                <Tooltip title="Eliminar avance">
                  <IconButton size="small" onClick={() => setPorEliminar(p)} sx={{ color: 'error.main' }}>
                    <Trash2 size={17} />
                  </IconButton>
                </Tooltip>
              )}
            </>
          )
        }}
      />

      <Dialog
        open={enFormulario !== null}
        onClose={() => (guardando ? undefined : setEnFormulario(null))}
        maxWidth="xs"
        fullWidth
      >
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
            <TrendingUp size={20} />
          </Box>
          {avanceEditando ? 'Editar avance' : 'Registrar avance'}
        </DialogTitle>
        <DialogContent>
          <Stack spacing={2.5} sx={{ mt: 2 }}>
            {enFormulario && (
              <Box>
                <Typography
                  variant="caption"
                  color="text.secondary"
                  sx={{ fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em' }}
                >
                  Planeacion
                </Typography>
                <Typography variant="body2" sx={{ fontWeight: 600 }}>
                  {enFormulario.titulo}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {enFormulario.materiaCodigo} · {enFormulario.parcialNombre} ·{' '}
                  {formatearFecha(enFormulario.createdAt)}
                </Typography>
              </Box>
            )}
            <FormControl fullWidth disabled={guardando}>
              <InputLabel>Estado del avance</InputLabel>
              <Select
                value={estado}
                label="Estado del avance"
                onChange={(e) => setEstado(e.target.value as EstadoAvance)}
              >
                {ESTADOS_AVANCE.map((opcion) => (
                  <MenuItem key={opcion} value={opcion}>
                    {ETIQUETAS_AVANCE[opcion]}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <TextField
              label="Observaciones"
              fullWidth
              multiline
              minRows={3}
              value={observaciones}
              onChange={(e) => setObservaciones(e.target.value)}
              disabled={guardando}
              helperText="Opcional (max. 1000 caracteres)"
              slotProps={{ htmlInput: { maxLength: 1000 } }}
            />
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setEnFormulario(null)} color="inherit" disabled={guardando}>
            Cancelar
          </Button>
          <Button onClick={() => void guardar()} variant="contained" disabled={guardando}>
            {avanceEditando ? 'Guardar' : 'Registrar'}
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmarAccion
        abierto={porEliminar !== null}
        titulo="Eliminar avance"
        mensaje={
          porEliminar
            ? `Se eliminara el avance registrado para la planeacion "${porEliminar.titulo}".`
            : ''
        }
        textoConfirmar="Eliminar"
        color="error"
        onConfirmar={() => void confirmarEliminar()}
        onCerrar={() => setPorEliminar(null)}
      />
    </Box>
  )
}