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
  IconButton,
  InputAdornment,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from '@mui/material'
import dayjs from 'dayjs'
import { Award, Download, Paperclip, Pencil, Plus, Search, Trash2, X } from 'lucide-react'
import { useCallback, useEffect, useState } from 'react'
import { sileo } from 'sileo'
import { validarArchivo } from '../api/archivos'
import {
  actualizarEvidencia,
  crearEvidencia,
  descargarEvidencia,
  eliminarEvidencia,
  listarEvidencias,
} from '../api/evidencias'
import { TablaCatalogo, type ColumnaCatalogo } from '../components/TablaCatalogo'
import { ConfirmarAccion } from '../components/catalogo/ConfirmarAccion'
import { EncabezadoPagina } from '../components/catalogo/EncabezadoPagina'
import { PanelFiltros } from '../components/catalogo/PanelFiltros'
import { SubirArchivo } from '../components/catalogo/SubirArchivo'
import type { Evidencia, EvidenciaForm } from '../types/evidencias'

function mensajeDeError(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    const cuerpo = error.response?.data as { message?: string } | undefined
    if (cuerpo?.message) return cuerpo.message
  }
  return fallback
}

function formatearFecha(fecha: string): string {
  return dayjs(fecha).format('DD/MM/YYYY')
}

function formatearTamano(tamano: number): string {
  if (tamano < 1024) return `${tamano} B`
  if (tamano < 1024 * 1024) return `${(tamano / 1024).toFixed(1)} KB`
  return `${(tamano / (1024 * 1024)).toFixed(1)} MB`
}

export function EvidenciasPage() {
  const [evidencias, setEvidencias] = useState<Evidencia[]>([])
  const [total, setTotal] = useState(0)
  const [pagina, setPagina] = useState(0)
  const [tamano, setTamano] = useState(20)
  const [busqueda, setBusqueda] = useState('')
  const [cargando, setCargando] = useState(false)

  const [dialogoAbierto, setDialogoAbierto] = useState(false)
  const [editando, setEditando] = useState<Evidencia | null>(null)
  const [curso, setCurso] = useState('')
  const [institucion, setInstitucion] = useState('')
  const [fecha, setFecha] = useState('')
  const [horas, setHoras] = useState('')
  const [archivo, setArchivo] = useState<File | null>(null)
  const [errorArchivo, setErrorArchivo] = useState<string | null>(null)
  const [errores, setErrores] = useState<{
    curso?: string
    institucion?: string
    fecha?: string
    horas?: string
  }>({})
  const [guardando, setGuardando] = useState(false)
  const [porEliminar, setPorEliminar] = useState<Evidencia | null>(null)

  const cargar = useCallback(async () => {
    const datos = await listarEvidencias({
      q: busqueda || null,
      page: pagina,
      size: tamano,
    })
    setEvidencias(datos.content)
    setTotal(datos.totalElementos)
  }, [busqueda, pagina, tamano])

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
    if (pagina === 0) {
      void cargar()
    } else {
      setPagina(0)
    }
  }

  const abrirRegistro = () => {
    setEditando(null)
    setCurso('')
    setInstitucion('')
    setFecha('')
    setHoras('')
    setArchivo(null)
    setErrorArchivo(null)
    setErrores({})
    setDialogoAbierto(true)
  }

  const abrirEdicion = (evidencia: Evidencia) => {
    setEditando(evidencia)
    setCurso(evidencia.curso)
    setInstitucion(evidencia.institucion)
    setFecha(dayjs(evidencia.fecha).format('YYYY-MM-DD'))
    setHoras(String(evidencia.horasAcreditadas))
    setArchivo(null)
    setErrorArchivo(null)
    setErrores({})
    setDialogoAbierto(true)
  }

  const seleccionarArchivo = (seleccionado: File) => {
    const motivo = validarArchivo(seleccionado)
    if (motivo) {
      setErrorArchivo(motivo)
      setArchivo(null)
      return
    }
    setErrorArchivo(null)
    setArchivo(seleccionado)
  }

  const validar = () => {
    const hoy = dayjs().format('YYYY-MM-DD')
    const horasNumero = Number(horas)
    const nuevos: {
      curso?: string
      institucion?: string
      fecha?: string
      horas?: string
    } = {}
    if (!curso.trim()) nuevos.curso = 'El curso o capacitacion es obligatorio'
    if (!institucion.trim()) nuevos.institucion = 'La institucion es obligatoria'
    if (!fecha) {
      nuevos.fecha = 'La fecha es obligatoria'
    } else if (fecha > hoy) {
      nuevos.fecha = 'La fecha no puede ser futura'
    }
    if (horas.trim() === '') {
      nuevos.horas = 'Las horas acreditadas son obligatorias'
    } else if (!Number.isFinite(horasNumero) || horasNumero <= 0) {
      nuevos.horas = 'Las horas deben ser mayores a cero'
    }
    setErrores(nuevos)
    return Object.keys(nuevos).length === 0
  }

  const guardar = async () => {
    if (!validar()) {
      sileo.error({ title: 'Revisa los campos marcados en el formulario' })
      return
    }

    const payload: EvidenciaForm = {
      curso: curso.trim(),
      institucion: institucion.trim(),
      fecha,
      horasAcreditadas: Number(horas),
    }

    setGuardando(true)
    try {
      if (editando) {
        await actualizarEvidencia(editando.id, payload, archivo)
        sileo.success({ title: 'Evidencia actualizada' })
      } else {
        await crearEvidencia(payload, archivo)
        sileo.success({ title: 'Evidencia registrada' })
      }
      setDialogoAbierto(false)
      recargar()
    } catch (error) {
      sileo.error({ title: mensajeDeError(error, 'No se pudo guardar la evidencia') })
    } finally {
      setGuardando(false)
    }
  }

  const confirmarEliminar = async () => {
    if (!porEliminar) return
    const evidencia = porEliminar
    setPorEliminar(null)
    try {
      await eliminarEvidencia(evidencia.id)
      sileo.success({ title: 'Evidencia eliminada' })
      recargar()
    } catch (error) {
      sileo.error({ title: mensajeDeError(error, 'No se pudo eliminar la evidencia') })
    }
  }

  const descargar = async (evidencia: Evidencia) => {
    try {
      await descargarEvidencia(evidencia)
    } catch {
      sileo.error({ title: 'No se pudo descargar el archivo' })
    }
  }

  const columnas: ColumnaCatalogo<Evidencia>[] = [
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
    { encabezado: 'Fecha', render: (e) => formatearFecha(e.fecha) },
    {
      encabezado: 'Horas',
      alineacion: 'center',
      render: (e) => `${e.horasAcreditadas} h`,
    },
    {
      encabezado: 'Estado',
      render: (e) =>
        e.tieneArchivo ? (
          <Chip
            size="small"
            color="success"
            variant="outlined"
            icon={<Paperclip size={13} />}
            label="Con archivo"
          />
        ) : (
          <Chip size="small" variant="outlined" label="Sin archivo" />
        ),
    },
    {
      encabezado: 'Adjunto',
      render: (e) =>
        e.archivoNombre ? (
          <Box sx={{ minWidth: 0 }}>
            <Typography
              variant="body2"
              sx={{ overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: 200 }}
            >
              {e.archivoNombre}
            </Typography>
            {e.archivoTamano !== null && (
              <Typography variant="caption" color="text.secondary">
                {formatearTamano(e.archivoTamano)}
              </Typography>
            )}
          </Box>
        ) : (
          '—'
        ),
    },
    {
      encabezado: 'Actualizado',
      render: (e) => dayjs(e.updatedAt).format('DD/MM/YYYY HH:mm'),
    },
  ]

  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 1200, mx: 'auto' }}>
      <EncabezadoPagina
        titulo="Evidencias de capacitacion"
        descripcion="Registra y consulta los cursos de capacitacion con sus horas acreditadas y archivo opcional."
        icono={<Award size={26} />}
        accion={
          <Button variant="contained" startIcon={<Plus size={18} />} onClick={abrirRegistro}>
            Registrar evidencia
          </Button>
        }
      />

      <PanelFiltros onRecargar={recargar}>
        <TextField
          label="Buscar por curso o institucion"
          size="small"
          value={busqueda}
          onChange={(e) => {
            setPagina(0)
            setBusqueda(e.target.value)
          }}
          sx={{ minWidth: { sm: 240 }, flex: { sm: 1 } }}
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
        mensajeVacio="Aun no has registrado evidencias"
        detalleVacio="Registra tu primera capacitacion para llevar el control de tus horas acreditadas."
        acciones={(e) => (
          <>
            <Tooltip title="Editar">
              <IconButton size="small" onClick={() => abrirEdicion(e)} sx={{ color: 'primary.main' }}>
                <Pencil size={17} />
              </IconButton>
            </Tooltip>
            {e.tieneArchivo && (
              <Tooltip title="Descargar archivo">
                <IconButton size="small" onClick={() => void descargar(e)}>
                  <Download size={17} />
                </IconButton>
              </Tooltip>
            )}
            <Tooltip title="Eliminar">
              <IconButton size="small" onClick={() => setPorEliminar(e)} sx={{ color: 'error.main' }}>
                <Trash2 size={17} />
              </IconButton>
            </Tooltip>
          </>
        )}
      />

      <Dialog
        open={dialogoAbierto}
        onClose={() => (guardando ? undefined : setDialogoAbierto(false))}
        maxWidth="sm"
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
            <Award size={20} />
          </Box>
          {editando ? 'Editar evidencia' : 'Registrar evidencia'}
        </DialogTitle>
        <DialogContent>
          <Stack spacing={2.5} sx={{ mt: 2 }}>
            <TextField
              label="Curso o capacitacion"
              fullWidth
              required
              value={curso}
              onChange={(e) => {
                setCurso(e.target.value)
                if (errores.curso) setErrores((prev) => ({ ...prev, curso: undefined }))
              }}
              disabled={guardando}
              error={Boolean(errores.curso)}
              helperText={errores.curso}
              slotProps={{ htmlInput: { maxLength: 200 } }}
            />
            <TextField
              label="Institucion"
              fullWidth
              required
              value={institucion}
              onChange={(e) => {
                setInstitucion(e.target.value)
                if (errores.institucion) setErrores((prev) => ({ ...prev, institucion: undefined }))
              }}
              disabled={guardando}
              error={Boolean(errores.institucion)}
              helperText={errores.institucion}
              slotProps={{ htmlInput: { maxLength: 200 } }}
            />
            <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2.5}>
              <TextField
                label="Fecha"
                type="date"
                fullWidth
                required
                value={fecha}
                onChange={(e) => {
                  setFecha(e.target.value)
                  if (errores.fecha) setErrores((prev) => ({ ...prev, fecha: undefined }))
                }}
                disabled={guardando}
                error={Boolean(errores.fecha)}
                helperText={errores.fecha}
                slotProps={{ inputLabel: { shrink: true }, htmlInput: { max: dayjs().format('YYYY-MM-DD') } }}
              />
              <TextField
                label="Horas acreditadas"
                type="number"
                fullWidth
                required
                value={horas}
                onChange={(e) => {
                  setHoras(e.target.value)
                  if (errores.horas) setErrores((prev) => ({ ...prev, horas: undefined }))
                }}
                disabled={guardando}
                error={Boolean(errores.horas)}
                helperText={errores.horas}
                slotProps={{ htmlInput: { min: 1, step: 1 } }}
              />
            </Stack>
            <Box>
              <Typography
                variant="caption"
                color="text.secondary"
                sx={{ fontWeight: 700, textTransform: 'uppercase', letterSpacing: '0.05em', display: 'block', mb: 1 }}
              >
                Archivo adjunto (opcional)
              </Typography>
              {editando?.tieneArchivo && !archivo && (
                <Typography variant="body2" color="text.secondary" sx={{ mb: 1 }}>
                  Archivo actual: <strong>{editando.archivoNombre}</strong>. Selecciona uno nuevo para reemplazarlo.
                </Typography>
              )}
              <SubirArchivo
                onSeleccionar={seleccionarArchivo}
                error={errorArchivo}
                subiendo={guardando}
              />
            </Box>
          </Stack>
        </DialogContent>
        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setDialogoAbierto(false)} color="inherit" disabled={guardando}>
            Cancelar
          </Button>
          <Button onClick={() => void guardar()} variant="contained" disabled={guardando}>
            {editando ? 'Guardar cambios' : 'Registrar'}
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmarAccion
        abierto={porEliminar !== null}
        titulo="Eliminar evidencia"
        mensaje={
          porEliminar
            ? `Se eliminara la evidencia "${porEliminar.curso}"${porEliminar.tieneArchivo ? ' junto con su archivo adjunto' : ''}. Esta accion no se puede deshacer.`
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
