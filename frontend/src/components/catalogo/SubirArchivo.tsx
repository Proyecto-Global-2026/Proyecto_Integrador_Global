import Box from '@mui/material/Box'
import FormHelperText from '@mui/material/FormHelperText'
import Typography from '@mui/material/Typography'
import { alpha } from '@mui/material/styles'
import { Loader2, UploadCloud } from 'lucide-react'
import type { ReactNode } from 'react'
import { useRef, useState } from 'react'
import { ACCEPT_FORMATOS, MAXIMO_MB_ARCHIVO } from '../../api/archivos'

interface Props {
  onSeleccionar: (archivo: File) => void
  error: string | null
  subiendo: boolean
}

export function SubirArchivo({ onSeleccionar, error, subiendo }: Props) {
  const entrada = useRef<HTMLInputElement>(null)
  const [arrastrando, setArrastrando] = useState(false)
  const [elegido, setElegido] = useState('')

  const procesar = (archivo: File | null) => {
    if (!archivo || subiendo) return
    setElegido(archivo.name)
    onSeleccionar(archivo)
  }

  return (
    <>
      <input
        ref={entrada}
        type="file"
        accept={ACCEPT_FORMATOS}
        style={{ display: 'none' }}
        onChange={(e) => {
          procesar(e.target.files?.[0] ?? null)
          e.target.value = ''
        }}
      />
      <BoxDrop
        arrastrando={arrastrando}
        subiendo={subiendo}
        onAbrir={() => entrada.current?.click()}
        onArrastrar={setArrastrando}
        onSoltar={(archivo) => procesar(archivo)}
      >
        {subiendo ? <Loader2 size={22} className="animate-spin" /> : <UploadCloud size={22} />}
        <Typography variant="body2" sx={{ fontWeight: 600, mt: 1 }}>
          {subiendo ? 'Subiendo archivo...' : 'Arrastra tu archivo aqui o toca para seleccionar'}
        </Typography>
        {!subiendo && !elegido && (
          <Typography variant="caption" color="text.secondary" sx={{ textAlign: 'center' }}>
            {ACCEPT_FORMATOS.replaceAll(',', ' · ')} · maximo {MAXIMO_MB_ARCHIVO} MB
          </Typography>
        )}
        {!subiendo && elegido && (
          <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 600 }}>
            Seleccionado: {elegido}
          </Typography>
        )}
      </BoxDrop>
      {error && <FormHelperText error>{error}</FormHelperText>}
    </>
  )
}

function BoxDrop({
  children,
  arrastrando,
  subiendo,
  onAbrir,
  onArrastrar,
  onSoltar,
}: {
  children: ReactNode
  arrastrando: boolean
  subiendo: boolean
  onAbrir: () => void
  onArrastrar: (activo: boolean) => void
  onSoltar: (archivo: File | null) => void
}) {
  return (
    <Box
      role="button"
      tabIndex={0}
      sx={{
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 0.5,
        p: 2.5,
        borderWidth: 2,
        borderStyle: 'dashed',
        borderRadius: 3,
        borderColor: arrastrando ? 'primary.main' : 'divider',
        bgcolor: arrastrando ? (theme) => alpha(theme.palette.primary.main, 0.04) : 'transparent',
        color: arrastrando ? 'primary.main' : 'text.secondary',
        textAlign: 'center',
        cursor: subiendo ? 'default' : 'pointer',
        opacity: subiendo ? 0.65 : 1,
        '&:hover': { borderColor: 'primary.main', color: 'text.primary' },
      }}
      onClick={() => {
        if (!subiendo) onAbrir()
      }}
      onKeyDown={(e) => {
        if (e.key === 'Enter' && !subiendo) onAbrir()
      }}
      onDragOver={(e) => {
        e.preventDefault()
        onArrastrar(true)
      }}
      onDragLeave={() => onArrastrar(false)}
      onDrop={(e) => {
        e.preventDefault()
        onArrastrar(false)
        onSoltar(e.dataTransfer.files[0] ?? null)
      }}
    >
      {children}
    </Box>
  )
}