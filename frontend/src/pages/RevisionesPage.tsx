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
import { CheckCircle2, Clock, Eye, Search, X, XCircle } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { sileo } from 'sileo'
import { listarMaterias } from '../api/catalogo'
import { listarPlaneaciones } from '../api/planeaciones'
import { TablaCatalogo, type ColumnaCatalogo } from '../components/TablaCatalogo'
import { AccionesRevision } from '../components/catalogo/AccionesRevision'
import { ChipEstadoPlaneacion } from '../components/catalogo/ChipEstadoPlaneacion'
import { EncabezadoPagina } from '../components/catalogo/EncabezadoPagina'
import { HistorialRevisiones } from '../components/catalogo/HistorialRevisiones'
import { ListaArchivos } from '../components/catalogo/ListaArchivos'
import { PanelFiltros } from '../components/catalogo/PanelFiltros'
import { TarjetaEstadistica } from '../components/catalogo/TarjetaEstadistica'
import type { Materia } from '../types/catalogo'
import type { Planeacion } from '../types/planeaciones'

function formatearFecha(fecha: string): string {
  return dayjs(fecha).format('DD/MM/YYYY HH:mm')
}

export function RevisionesPage() {
  const [planeaciones, setPlaneaciones] = useState<Planeacion[]>([])
  const [materias, setMaterias] = useState<Materia[]>([])
  const [total, setTotal] = useState(0)
  const [pagina, setPagina] = useState(0)
  const [tamano, setTamano] = useState(20)
  const [busqueda, setBusqueda] = useState('')
  const [filtroMateria, setFiltroMateria] = useState('')
  const [cargando, setCargando] = useState(false)
  const [enDetalle, setEnDetalle] = useState<Planeacion | null>(null)
  const [conteos, setConteos] = useState({ pendientes: 0, aprobadas: 0, rechazadas: 0 })

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

  const obtenerConteos = useCallback(async () => {
    const [pendientes, aprobadas, rechazadas] = await Promise.all([
      listarPlaneaciones({ estado: 'PENDIENTE', page: 0, size: 1 }),
      listarPlaneaciones({ estado: 'APROBADA', page: 0, size: 1 }),
      listarPlaneaciones({ estado: 'RECHAZADA', page: 0, size: 1 }),
    ])
    return {
      pendientes: pendientes.totalElementos,
      aprobadas: aprobadas.totalElementos,
      rechazadas: rechazadas.totalElementos,
    }
  }, [])

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      try {
        const datos = await obtenerConteos()
        if (!cancelado) setConteos(datos)
      } catch {
        if (!cancelado) setConteos({ pendientes: 0, aprobadas: 0, rechazadas: 0 })
      }
    }
    void ejecutar()
    return () => {
      cancelado = true
    }
  }, [obtenerConteos])

  const refrescarConteos = () => {
    void obtenerConteos()
      .then(setConteos)
      .catch(() => setConteos({ pendientes: 0, aprobadas: 0, rechazadas: 0 }))
  }

  const cargar = useCallback(async () => {
    const datos = await listarPlaneaciones({
      q: busqueda || null,
      estado: 'PENDIENTE',
      materiaId: filtroMateria || null,
      page: pagina,
      size: tamano,
    })
    setPlaneaciones(datos.content)
    setTotal(datos.totalElementos)
  }, [busqueda, filtroMateria, pagina, tamano])

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      setCargando(true)
      try {
        if (!cancelado) await cargar()
      } catch {
        if (!cancelado) sileo.error({ title: 'No se pudieron cargar las planeaciones pendientes' })
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
    refrescarConteos()
    if (pagina === 0) {
      void cargar()
    } else {
      setPagina(0)
    }
  }

  const abrirDetalle = (planeacion: Planeacion) => {
    setEnDetalle(planeacion)
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
    { encabezado: 'Parcial', render: (p) => p.parcialNombre },
    { encabezado: 'Enviada', render: (p) => formatearFecha(p.createdAt) },
  ]

  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 1200, mx: 'auto' }}>
      <EncabezadoPagina
        titulo="Revision de planeaciones"
        descripcion="Revisa las planeaciones pendientes y registra tu decision."
        icono={<CheckCircle2 size={26} />}
      />

      <Box
        sx={{
          display: 'grid',
          gap: 2,
          gridTemplateColumns: { xs: '1fr', sm: 'repeat(3, 1fr)' },
          mb: 3,
        }}
      >
        <TarjetaEstadistica etiqueta="Pendientes" valor={conteos.pendientes} icono={<Clock size={22} />} variedad="primario" />
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
        mensajeVacio="No hay planeaciones pendientes de revision"
        detalleVacio="Cuando un docente envie una planeacion, aparecera aqui para su revision."
        acciones={(p) => (
          <Tooltip title="Revisar">
            <IconButton size="small" onClick={() => abrirDetalle(p)} sx={{ color: 'primary.main' }}>
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
            <CheckCircle2 size={20} />
          </Box>
          Revisar planeacion
        </DialogTitle>
        <DialogContent>
          {enDetalle && (
            <Stack spacing={2.5} sx={{ mt: 2 }}>
              <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between', gap: 1, flexWrap: 'wrap' }}>
                <ChipEstadoPlaneacion estado={enDetalle.estado} />
                <Typography variant="caption" color="text.secondary">
                  Enviada {formatearFecha(enDetalle.createdAt)}
                </Typography>
              </Stack>
              <DetalleCampo etiqueta="Docente" valor={enDetalle.docenteNombre} />
              <DetalleCampo etiqueta="Materia" valor={`${enDetalle.materiaCodigo} · ${enDetalle.materiaNombre}`} />
              <DetalleCampo etiqueta="Parcial" valor={enDetalle.parcialNombre} />
              <DetalleCampo etiqueta="Titulo" valor={enDetalle.titulo} />
              <DetalleCampo etiqueta="Contenido" valor={enDetalle.contenido} />
              <HistorialRevisiones planeacionId={enDetalle.id} />
              <ListaArchivos planeacionId={enDetalle.id} />
              <AccionesRevision
                planeacionId={enDetalle.id}
                titulo={enDetalle.titulo}
                onRevisado={() => {
                  setEnDetalle(null)
                  recargar()
                }}
              />
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