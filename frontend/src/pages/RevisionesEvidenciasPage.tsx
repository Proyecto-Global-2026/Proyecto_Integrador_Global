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
import {
  BadgeCheck,
  CheckCircle2,
  Clock,
  Download,
  Eye,
  FileText,
  Search,
  X,
  XCircle,
} from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { sileo } from 'sileo'
import { descargarEvidencia, listarEvidencias, revisarEvidencia } from '../api/evidencias'
import { listarUsuarios } from '../api/usuarios'
import { TablaCatalogo, type ColumnaCatalogo } from '../components/TablaCatalogo'
import { ChipEstadoEvidencia } from '../components/catalogo/ChipEstadoEvidencia'
import { ConfirmarAccion } from '../components/catalogo/ConfirmarAccion'
import { EncabezadoPagina } from '../components/catalogo/EncabezadoPagina'
import { HistorialRevisionesEvidencia } from '../components/catalogo/HistorialRevisionesEvidencia'
import { PanelFiltros } from '../components/catalogo/PanelFiltros'
import { TarjetaEstadistica } from '../components/catalogo/TarjetaEstadistica'
import {
  ESTADOS_EVIDENCIA,
  ETIQUETAS_EVIDENCIA,
  type DecisionEvidencia,
  type EstadoEvidencia,
  type Evidencia,
} from '../types/evidencias'
import type { Usuario } from '../types/usuario'

const TEXTO_DECISION: Record<DecisionEvidencia, string> = {
  APROBADA: 'Aprobar evidencia',
  RECHAZADA: 'Rechazar evidencia',
}

function formatearFecha(fecha: string): string {
  return dayjs(fecha).format('DD/MM/YYYY HH:mm')
}

function formatearTamano(bytes: number | null): string {
  if (!bytes) return ''
  if (bytes < 1024) return `${bytes} B`
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`
}

function mensajeDeError(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const cuerpo = error.response?.data as { message?: string } | undefined
    if (cuerpo?.message) return cuerpo.message
  }
  return 'No se pudo registrar la revision'
}

export function RevisionesEvidenciasPage() {
  const [evidencias, setEvidencias] = useState<Evidencia[]>([])
  const [docentes, setDocentes] = useState<Usuario[]>([])
  const [total, setTotal] = useState(0)
  const [pagina, setPagina] = useState(0)
  const [tamano, setTamano] = useState(20)
  const [busqueda, setBusqueda] = useState('')
  const [filtroDocente, setFiltroDocente] = useState('')
  const [filtroEstado, setFiltroEstado] = useState<EstadoEvidencia | ''>('PENDIENTE')
  const [cargando, setCargando] = useState(false)
  const [conteos, setConteos] = useState({ pendientes: 0, aprobadas: 0, rechazadas: 0 })

  const [enDetalle, setEnDetalle] = useState<Evidencia | null>(null)
  const [comentario, setComentario] = useState('')
  const [errorComentario, setErrorComentario] = useState<string | null>(null)
  const [decision, setDecision] = useState<DecisionEvidencia | null>(null)
  const [enviando, setEnviando] = useState(false)

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      try {
        const datos = await listarUsuarios({ rol: 'DOCENTE', activo: true, size: 100, sort: 'nombre' })
        if (!cancelado) setDocentes(datos.content)
      } catch {
        if (!cancelado) setDocentes([])
      }
    }
    void ejecutar()
    return () => {
      cancelado = true
    }
  }, [])

  const obtenerConteos = useCallback(async () => {
    const [pendientes, aprobadas, rechazadas] = await Promise.all([
      listarEvidencias({ estado: 'PENDIENTE', page: 0, size: 1 }),
      listarEvidencias({ estado: 'APROBADA', page: 0, size: 1 }),
      listarEvidencias({ estado: 'RECHAZADA', page: 0, size: 1 }),
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
    const datos = await listarEvidencias({
      q: busqueda || null,
      estado: filtroEstado || null,
      usuarioId: filtroDocente || null,
      page: pagina,
      size: tamano,
    })
    setEvidencias(datos.content)
    setTotal(datos.totalElementos)
  }, [busqueda, filtroEstado, filtroDocente, pagina, tamano])

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      setCargando(true)
      try {
        if (!cancelado) await cargar()
      } catch {
        if (!cancelado) sileo.error({ title: 'No se pudieron cargar las evidencias' })
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

  const abrirDetalle = (evidencia: Evidencia) => {
    setComentario('')
    setErrorComentario(null)
    setEnDetalle(evidencia)
  }

  const solicitar = (estado: DecisionEvidencia) => {
    if (estado === 'RECHAZADA' && !comentario.trim()) {
      setErrorComentario('El comentario es obligatorio al rechazar una evidencia')
      return
    }
    setErrorComentario(null)
    setDecision(estado)
  }

  const confirmar = async () => {
    if (!decision || !enDetalle) return
    const estado = decision
    setEnviando(true)
    try {
      await revisarEvidencia(enDetalle.id, {
        estado,
        comentario: comentario.trim() ? comentario.trim() : undefined,
      })
      sileo.success({ title: `${TEXTO_DECISION[estado]} registrada` })
      setDecision(null)
      setEnDetalle(null)
      recargar()
    } catch (error) {
      setDecision(null)
      sileo.error({ title: mensajeDeError(error) })
    } finally {
      setEnviando(false)
    }
  }

  const descargar = async (evidencia: Evidencia) => {
    try {
      await descargarEvidencia(evidencia)
    } catch {
      sileo.error({ title: 'No se pudo descargar el archivo adjunto' })
    }
  }

  const columnas: ColumnaCatalogo<Evidencia>[] = [
    { encabezado: 'Docente', render: (e) => e.usuarioNombre },
    {
      encabezado: 'Curso',
      render: (e) => (
        <Box sx={{ minWidth: 0 }}>
          <Typography variant="body2" sx={{ fontWeight: 600 }}>
            {e.curso}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {e.institucion}
          </Typography>
        </Box>
      ),
    },
    { encabezado: 'Fecha', render: (e) => dayjs(e.fecha).format('DD/MM/YYYY') },
    { encabezado: 'Horas', render: (e) => e.horasAcreditadas, alineacion: 'center' },
    { encabezado: 'Estado', render: (e) => <ChipEstadoEvidencia estado={e.estado} /> },
    {
      encabezado: 'Adjunto',
      render: (e) =>
        e.tieneArchivo ? (
          <Chip size="small" color="info" variant="outlined" icon={<FileText size={14} />} label="Con archivo" />
        ) : (
          <Chip size="small" variant="outlined" label="Sin archivo" />
        ),
    },
  ]

  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 1200, mx: 'auto' }}>
      <EncabezadoPagina
        titulo="Revision de evidencias"
        descripcion="Revisa las evidencias de capacitacion y aprueba o rechaza su validez."
        icono={<BadgeCheck size={26} />}
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
          label="Buscar por curso"
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
        <FormControl size="small" sx={{ minWidth: 160 }}>
          <InputLabel>Estado</InputLabel>
          <Select
            value={filtroEstado}
            label="Estado"
            onChange={(e) => {
              setPagina(0)
              setFiltroEstado(e.target.value as EstadoEvidencia | '')
            }}
          >
            <MenuItem value="">Todos</MenuItem>
            {ESTADOS_EVIDENCIA.map((estado) => (
              <MenuItem key={estado} value={estado}>
                {ETIQUETAS_EVIDENCIA[estado]}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      </PanelFiltros>

      <TablaCatalogo
        columnas={columnas}
        filas={evidencias}
        cargando={cargando}
        total={total}
        pagina={pagina}
        tamanoPagina={tamano}
        onCambiarPagina={setPagina}
        onCambiarTamano={setTamano}
        claveFila={(e) => e.id}
        mensajeVacio="No hay evidencias que revisar"
        detalleVacio="Cuando un docente registre una evidencia, aparecera aqui para su revision."
        acciones={(e) => (
          <Tooltip title="Revisar">
            <IconButton size="small" onClick={() => abrirDetalle(e)} sx={{ color: 'primary.main' }}>
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
            <BadgeCheck size={20} />
          </Box>
          Revisar evidencia
        </DialogTitle>
        <DialogContent>
          {enDetalle && (
            <Stack spacing={2.5} sx={{ mt: 2 }}>
              <Stack direction="row" sx={{ alignItems: 'center', justifyContent: 'space-between', gap: 1, flexWrap: 'wrap' }}>
                <ChipEstadoEvidencia estado={enDetalle.estado} />
                <Typography variant="caption" color="text.secondary">
                  Registrada {formatearFecha(enDetalle.createdAt)}
                </Typography>
              </Stack>
              <DetalleCampo etiqueta="Docente" valor={enDetalle.usuarioNombre} />
              <DetalleCampo etiqueta="Curso o capacitacion" valor={enDetalle.curso} />
              <DetalleCampo etiqueta="Institucion" valor={enDetalle.institucion} />
              <DetalleCampo etiqueta="Fecha" valor={dayjs(enDetalle.fecha).format('DD/MM/YYYY')} />
              <DetalleCampo etiqueta="Horas acreditadas" valor={String(enDetalle.horasAcreditadas)} />

              <Box>
                <Typography
                  variant="caption"
                  color="text.secondary"
                  sx={{ fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em' }}
                >
                  Archivo adjunto
                </Typography>
                {enDetalle.tieneArchivo ? (
                  <Stack
                    direction="row"
                    spacing={1.5}
                    sx={{
                      mt: 1,
                      p: 1.5,
                      borderRadius: 2,
                      border: '1px solid',
                      borderColor: 'divider',
                      alignItems: 'center',
                    }}
                  >
                    <Box
                      sx={{
                        width: 38,
                        height: 38,
                        flexShrink: 0,
                        borderRadius: '10px',
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        color: 'info.main',
                        backgroundColor: (theme) => alpha(theme.palette.info.main, 0.12),
                      }}
                    >
                      <FileText size={19} />
                    </Box>
                    <Box sx={{ minWidth: 0, flex: 1 }}>
                      <Typography variant="body2" sx={{ fontWeight: 600 }} noWrap>
                        {enDetalle.archivoNombre ?? 'Archivo adjunto'}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {formatearTamano(enDetalle.archivoTamano)}
                      </Typography>
                    </Box>
                    <Button
                      size="small"
                      startIcon={<Download size={16} />}
                      onClick={() => void descargar(enDetalle)}
                    >
                      Descargar
                    </Button>
                  </Stack>
                ) : (
                  <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
                    Sin archivo adjunto.
                  </Typography>
                )}
              </Box>

              <HistorialRevisionesEvidencia evidenciaId={enDetalle.id} />

              {enDetalle.estado === 'PENDIENTE' && (
                <TextField
                  label="Comentario de revision"
                  fullWidth
                  multiline
                  minRows={3}
                  value={comentario}
                  onChange={(e) => {
                    setComentario(e.target.value)
                    if (errorComentario) setErrorComentario(null)
                  }}
                  error={!!errorComentario}
                  helperText={errorComentario ?? 'Obligatorio al rechazar. Opcional al aprobar.'}
                  slotProps={{ htmlInput: { maxLength: 1000 } }}
                />
              )}
            </Stack>
          )}
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setEnDetalle(null)} color="inherit">
            Cerrar
          </Button>
          {enDetalle?.estado === 'PENDIENTE' && (
            <>
              <Button
                onClick={() => solicitar('RECHAZADA')}
                color="error"
                variant="outlined"
                startIcon={<XCircle size={16} />}
                disabled={enviando}
              >
                Rechazar
              </Button>
              <Button
                onClick={() => solicitar('APROBADA')}
                color="success"
                variant="contained"
                startIcon={<CheckCircle2 size={16} />}
                disabled={enviando}
              >
                Aprobar
              </Button>
            </>
          )}
        </DialogActions>
      </Dialog>

      <ConfirmarAccion
        abierto={decision !== null}
        titulo={decision ? TEXTO_DECISION[decision] : ''}
        mensaje={
          decision === 'APROBADA'
            ? `Se aprobara la evidencia "${enDetalle?.curso ?? ''}".`
            : `Se rechazara la evidencia "${enDetalle?.curso ?? ''}".`
        }
        textoConfirmar={decision === 'APROBADA' ? 'Aprobar' : 'Rechazar'}
        color={decision === 'RECHAZADA' ? 'error' : 'success'}
        onConfirmar={() => void confirmar()}
        onCerrar={() => setDecision(null)}
      />
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
