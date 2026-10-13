import axios from 'axios'
import Box from '@mui/material/Box'
import IconButton from '@mui/material/IconButton'
import Stack from '@mui/material/Stack'
import Tooltip from '@mui/material/Tooltip'
import Typography from '@mui/material/Typography'
import dayjs from 'dayjs'
import { Download, FileImage, FileSpreadsheet, FileText, Paperclip, Trash2 } from 'lucide-react'
import type { ReactNode } from 'react'
import { useCallback, useEffect, useState } from 'react'
import { sileo } from 'sileo'
import {
  descargarArchivo,
  eliminarArchivo,
  extensionDeNombre,
  listarArchivos,
  subirArchivo,
  validarArchivo,
} from '../../api/archivos'
import type { ArchivoPlaneacion } from '../../types/archivos'
import { ConfirmarAccion } from './ConfirmarAccion'
import { SubirArchivo } from './SubirArchivo'

interface Props {
  planeacionId: string
  puedeSubir?: boolean
  puedeEliminar?: boolean
}

export function ListaArchivos({ planeacionId, puedeSubir = false, puedeEliminar = false }: Props) {
  const [archivos, setArchivos] = useState<ArchivoPlaneacion[]>([])
  const [cargando, setCargando] = useState(false)
  const [subiendo, setSubiendo] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [aEliminar, setAEliminar] = useState<ArchivoPlaneacion | null>(null)

  const cargar = useCallback(async () => {
    setArchivos(await listarArchivos(planeacionId))
  }, [planeacionId])

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      setCargando(true)
      try {
        if (!cancelado) await cargar()
      } catch {
        if (!cancelado) {
          setArchivos([])
          sileo.error({ title: 'No se pudieron cargar los archivos' })
        }
      } finally {
        if (!cancelado) setCargando(false)
      }
    }
    void ejecutar()
    return () => {
      cancelado = true
    }
  }, [cargar])

  const adjuntar = async (archivo: File) => {
    if (subiendo) return
    const motivo = validarArchivo(archivo)
    if (motivo) {
      setError(motivo)
      return
    }
    setError(null)
    setSubiendo(true)
    try {
      const creado = await subirArchivo(planeacionId, archivo)
      setArchivos((prev) => [...prev, creado])
      sileo.success({ title: 'Archivo adjuntado a la planeacion' })
    } catch (fallo) {
      sileo.error({ title: mensajeDeError(fallo, 'No se pudo adjuntar el archivo') })
    } finally {
      setSubiendo(false)
    }
  }

  const descargar = async (archivo: ArchivoPlaneacion) => {
    try {
      await descargarArchivo(planeacionId, archivo)
    } catch {
      sileo.error({ title: 'No se pudo descargar el archivo' })
    }
  }

  const eliminarConfirmado = async () => {
    if (!aEliminar) return
    const archivo = aEliminar
    setAEliminar(null)
    try {
      await eliminarArchivo(planeacionId, archivo.id)
      setArchivos((prev) => prev.filter((actual) => actual.id !== archivo.id))
      sileo.success({ title: 'Archivo eliminado' })
    } catch (fallo) {
      sileo.error({ title: mensajeDeError(fallo, 'No se pudo eliminar el archivo') })
    }
  }

  return (
    <Box>
      <Typography
        variant="caption"
        color="text.secondary"
        sx={{
          fontWeight: 700,
          textTransform: 'uppercase',
          letterSpacing: '0.05em',
          display: 'flex',
          alignItems: 'center',
          gap: 0.75,
        }}
      >
        <Paperclip size={14} /> Archivos adjuntos
      </Typography>

      {puedeSubir && (
        <Box sx={{ mt: 1 }}>
          <SubirArchivo onSeleccionar={(archivo) => void adjuntar(archivo)} error={error} subiendo={subiendo} />
        </Box>
      )}

      <Stack spacing={1} sx={{ mt: 1.5 }}>
        {cargando && (
          <Typography variant="body2" color="text.secondary">
            Cargando archivos...
          </Typography>
        )}
        {!cargando && archivos.length === 0 && (
          <Typography variant="body2" color="text.secondary">
            {puedeSubir
              ? 'Aun no has adjuntado archivos a esta planeacion.'
              : 'Esta planeacion no tiene archivos adjuntos.'}
          </Typography>
        )}
        {!cargando &&
          archivos.map((archivo) => (
            <FilaArchivo
              key={archivo.id}
              archivo={archivo}
              puedeEliminar={puedeEliminar}
              onDescargar={() => void descargar(archivo)}
              onEliminar={() => setAEliminar(archivo)}
            />
          ))}
      </Stack>

      <ConfirmarAccion
        abierto={aEliminar !== null}
        titulo="Eliminar archivo adjunto"
        mensaje={
          <>
            Se eliminara <strong>{aEliminar?.nombreOriginal ?? 'el archivo adjunto'}</strong> de la planeacion.
            Esta accion no se puede deshacer.
          </>
        }
        textoConfirmar="Eliminar"
        color="error"
        onConfirmar={() => void eliminarConfirmado()}
        onCerrar={() => setAEliminar(null)}
      />
    </Box>
  )
}

function FilaArchivo({
  archivo,
  puedeEliminar,
  onDescargar,
  onEliminar,
}: {
  archivo: ArchivoPlaneacion
  puedeEliminar: boolean
  onDescargar: () => void
  onEliminar: () => void
}) {
  return (
    <Box
      sx={{
        display: 'flex',
        alignItems: 'center',
        gap: 1.5,
        p: 1.25,
        borderRadius: 2,
        border: '1px solid',
        borderColor: 'divider',
        bgcolor: 'background.paper',
      }}
    >
      <Box sx={{ display: 'flex', color: 'primary.main' }}>{iconoDeArchivo(archivo.nombreOriginal)}</Box>
      <Box sx={{ minWidth: 0, flex: 1 }}>
        <Typography
          variant="body2"
          sx={{ fontWeight: 600, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}
        >
          {archivo.nombreOriginal}
        </Typography>
        <Typography variant="caption" color="text.secondary">
          {formatearTamano(archivo.tamano)} · {dayjs(archivo.createdAt).format('DD/MM/YYYY HH:mm')}
        </Typography>
      </Box>
      <Tooltip title="Descargar">
        <IconButton size="small" onClick={onDescargar}>
          <Download size={17} />
        </IconButton>
      </Tooltip>
      {puedeEliminar && (
        <Tooltip title="Eliminar">
          <IconButton size="small" color="error" onClick={onEliminar}>
            <Trash2 size={17} />
          </IconButton>
        </Tooltip>
      )}
    </Box>
  )
}

function iconoDeArchivo(nombre: string): ReactNode {
  const extension = extensionDeNombre(nombre)
  if (extension === 'jpg' || extension === 'jpeg' || extension === 'png') return <FileImage size={20} />
  if (extension === 'xls' || extension === 'xlsx') return <FileSpreadsheet size={20} />
  return <FileText size={20} />
}

function formatearTamano(tamano: number): string {
  if (tamano < 1024) return `${tamano} B`
  if (tamano < 1024 * 1024) return `${(tamano / 1024).toFixed(1)} KB`
  return `${(tamano / (1024 * 1024)).toFixed(1)} MB`
}

function mensajeDeError(error: unknown, fallback: string): string {
  if (axios.isAxiosError(error)) {
    const cuerpo = error.response?.data as { message?: string } | undefined
    if (cuerpo?.message) return cuerpo.message
  }
  return fallback
}