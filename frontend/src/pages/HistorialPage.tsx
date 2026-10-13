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
import dayjs from 'dayjs'
import { Eye, History, Search, X } from 'lucide-react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { sileo } from 'sileo'
import { listarMaterias, listarParciales, listarPeriodos } from '../api/catalogo'
import { listarHistorial } from '../api/planeaciones'
import { listarUsuarios } from '../api/usuarios'
import { TablaCatalogo, type ColumnaCatalogo } from '../components/TablaCatalogo'
import { ChipEstadoPlaneacion } from '../components/catalogo/ChipEstadoPlaneacion'
import { EncabezadoPagina } from '../components/catalogo/EncabezadoPagina'
import { HistorialRevisiones } from '../components/catalogo/HistorialRevisiones'
import { ListaArchivos } from '../components/catalogo/ListaArchivos'
import { PanelFiltros } from '../components/catalogo/PanelFiltros'
import type { Materia, Parcial, Periodo } from '../types/catalogo'
import type { Planeacion } from '../types/planeaciones'
import type { Usuario } from '../types/usuario'

function formatearFecha(fecha: string): string {
  return dayjs(fecha).format('DD/MM/YYYY HH:mm')
}

export function HistorialPage() {
  const [planeaciones, setPlaneaciones] = useState<Planeacion[]>([])
  const [periodos, setPeriodos] = useState<Periodo[]>([])
  const [materias, setMaterias] = useState<Materia[]>([])
  const [docentes, setDocentes] = useState<Usuario[]>([])
  const [parciales, setParciales] = useState<Parcial[]>([])
  const [total, setTotal] = useState(0)
  const [pagina, setPagina] = useState(0)
  const [tamano, setTamano] = useState(20)
  const [busqueda, setBusqueda] = useState('')
  const [filtroPeriodo, setFiltroPeriodo] = useState('')
  const [filtroDocente, setFiltroDocente] = useState('')
  const [filtroMateria, setFiltroMateria] = useState('')
  const [cargando, setCargando] = useState(false)
  const [enDetalle, setEnDetalle] = useState<Planeacion | null>(null)

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      try {
        const [listaPeriodos, listaMaterias, listaDocentes, listaParciales] = await Promise.all([
          listarPeriodos({ size: 100, sort: 'fechaInicio,desc' }),
          listarMaterias({ size: 100, sort: 'nombre' }),
          listarUsuarios({ rol: 'DOCENTE', size: 100, sort: 'nombre' }),
          listarParciales({ size: 100, sort: 'fechaInicio,desc' }),
        ])
        if (!cancelado) {
          setPeriodos(listaPeriodos.content)
          setMaterias(listaMaterias.content)
          setDocentes(listaDocentes.content)
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

  const cicloPorParcial = useMemo(() => {
    const mapa = new Map<string, string>()
    parciales.forEach((p) => {
      if (p.periodoNombre) mapa.set(p.id, p.periodoNombre)
    })
    return mapa
  }, [parciales])

  const cargar = useCallback(async () => {
    const datos = await listarHistorial({
      q: busqueda || null,
      periodoId: filtroPeriodo || null,
      docenteId: filtroDocente || null,
      materiaId: filtroMateria || null,
      page: pagina,
      size: tamano,
    })
    setPlaneaciones(datos.content)
    setTotal(datos.totalElementos)
  }, [busqueda, filtroPeriodo, filtroDocente, filtroMateria, pagina, tamano])

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      setCargando(true)
      try {
        if (!cancelado) await cargar()
      } catch {
        if (!cancelado) sileo.error({ title: 'No se pudo cargar el historial de planeaciones' })
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

  const columnas: ColumnaCatalogo<Planeacion>[] = [
    { encabezado: 'Docente', render: (p) => p.docenteNombre },
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
    { encabezado: 'Ciclo', render: (p) => cicloPorParcial.get(p.parcialId) ?? '—' },
    { encabezado: 'Parcial', render: (p) => p.parcialNombre },
    { encabezado: 'Estado', render: (p) => <ChipEstadoPlaneacion estado={p.estado} /> },
    { encabezado: 'Actualizada', render: (p) => formatearFecha(p.updatedAt) },
  ]

  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 1200, mx: 'auto' }}>
      <EncabezadoPagina
        titulo="Historial de planeaciones"
        descripcion="Consulta las planeaciones de ciclos anteriores filtrando por ciclo, docente y materia."
        icono={<History size={26} />}
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
        <FormControl size="small" sx={{ minWidth: 180 }}>
          <InputLabel>Ciclo</InputLabel>
          <Select
            value={filtroPeriodo}
            label="Ciclo"
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
        <FormControl size="small" sx={{ minWidth: 200 }}>
          <InputLabel>Docente</InputLabel>
          <Select
            value={filtroDocente}
            label="Docente"
            onChange={(e) => {
              setPagina(0)
              setFiltroDocente(e.target.value)
            }}
          >
            <MenuItem value="">Todos</MenuItem>
            {docentes.map((d) => (
              <MenuItem key={d.id} value={d.id}>
                {d.nombre}
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
        mensajeVacio="No hay planeaciones en el historial"
        detalleVacio="Ajusta los filtros de ciclo, docente o materia para consultar resultados."
        acciones={(p) => (
          <Tooltip title="Ver detalle">
            <IconButton size="small" onClick={() => setEnDetalle(p)} sx={{ color: 'primary.main' }}>
              <Eye size={17} />
            </IconButton>
          </Tooltip>
        )}
      />

      <Dialog
        open={enDetalle !== null}
        onClose={() => setEnDetalle(null)}
        maxWidth="sm"
        fullWidth
        sx={{ '& .MuiDialog-paper': { maxHeight: '90vh' } }}
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
            <History size={20} />
          </Box>
          Detalle de la planeacion
        </DialogTitle>
        <DialogContent>
          {enDetalle && (
            <Stack spacing={2.5} sx={{ mt: 2 }}>
              <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between', gap: 1, flexWrap: 'wrap' }}>
                <ChipEstadoPlaneacion estado={enDetalle.estado} />
                <Typography variant="caption" color="text.secondary">
                  Actualizada {formatearFecha(enDetalle.updatedAt)}
                </Typography>
              </Stack>
              <DetalleCampo etiqueta="Docente" valor={enDetalle.docenteNombre} />
              <DetalleCampo etiqueta="Materia" valor={`${enDetalle.materiaCodigo} · ${enDetalle.materiaNombre}`} />
              <DetalleCampo etiqueta="Ciclo" valor={cicloPorParcial.get(enDetalle.parcialId) ?? '—'} />
              <DetalleCampo etiqueta="Parcial" valor={enDetalle.parcialNombre} />
              <DetalleCampo etiqueta="Titulo" valor={enDetalle.titulo} />
              <DetalleCampo etiqueta="Contenido" valor={enDetalle.contenido} />
              <HistorialRevisiones planeacionId={enDetalle.id} />
              <ListaArchivos planeacionId={enDetalle.id} />
            </Stack>
          )}
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setEnDetalle(null)} color="inherit">
            Cerrar
          </Button>
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