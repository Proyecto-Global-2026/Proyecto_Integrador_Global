import Box from '@mui/material/Box'
import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import FormControl from '@mui/material/FormControl'
import InputLabel from '@mui/material/InputLabel'
import MenuItem from '@mui/material/MenuItem'
import Select from '@mui/material/Select'
import Stack from '@mui/material/Stack'
import Typography from '@mui/material/Typography'
import { alpha, useTheme } from '@mui/material/styles'
import Button from '@mui/material/Button'
import { BarChart3, ClipboardCheck, History, RefreshCw, TrendingUp, Users } from 'lucide-react'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { sileo } from 'sileo'
import { obtenerReporteAvances, obtenerReporteCursos, obtenerReportePlaneaciones } from '../api/reportes'
import { listarPlaneacionesConsolidado } from '../api/planeaciones'
import { listarPeriodos } from '../api/catalogo'
import { TarjetaEstadistica } from '../components/catalogo/TarjetaEstadistica'
import { EncabezadoPagina } from '../components/catalogo/EncabezadoPagina'
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts'
import type { Periodo } from '../types/catalogo'
import type { Planeacion } from '../types/planeaciones'
import type { ReporteAvances, ReporteCursos, ReportePlaneaciones } from '../types/reportes'

export function DashboardPage() {
  const [periodos, setPeriodos] = useState<Periodo[]>([])
  const [periodoId, setPeriodoId] = useState<string>('')
  const [cargando, setCargando] = useState(false)
  const theme = useTheme()

  const [repPlaneaciones, setRepPlaneaciones] = useState<ReportePlaneaciones | null>(null)
  const [repAvances, setRepAvances] = useState<ReporteAvances | null>(null)
  const [repCursos, setRepCursos] = useState<ReporteCursos | null>(null)
  const [consolidado, setConsolidado] = useState<Planeacion[]>([])

  useEffect(() => {
    let cancelado = false
    const ejecutar = async () => {
      try {
        const datos = await listarPeriodos({ activo: true, size: 100, sort: 'fechaInicio,desc' })
        if (!cancelado) {
          setPeriodos(datos.content)
          if (datos.content.length > 0) setPeriodoId(datos.content[0].id)
        }
      } catch {
        if (!cancelado) setPeriodos([])
      }
    }
    void ejecutar()
    return () => {
      cancelado = true
    }
  }, [])

  const cargar = useCallback(async () => {
    setCargando(true)
    try {
      const [rp, ra, rc, cons] = await Promise.all([
        obtenerReportePlaneaciones(periodoId || null),
        obtenerReporteAvances(periodoId || null),
        obtenerReporteCursos(periodoId || null),
        listarPlaneacionesConsolidado(periodoId || null, 1000),
      ])
      setRepPlaneaciones(rp)
      setRepAvances(ra)
      setRepCursos(rc)
      setConsolidado(cons)
    } catch {
      sileo.error({ title: 'No se pudieron cargar los reportes' })
    } finally {
      setCargando(false)
    }
  }, [periodoId])

  useEffect(() => {
    let cancelado = false
    void (async () => {
      try {
        if (!cancelado) await cargar()
      } catch {
        // ignorar
      }
    })()
    return () => {
      cancelado = true
    }
  }, [cargar])

  const datosPorProfesor = useMemo(() => {
    const map = new Map<string, { total: number; aprobadas: number }>()
    for (const p of consolidado) {
      const key = p.docenteNombre || p.docenteId
      const acc = map.get(key) || { total: 0, aprobadas: 0 }
      acc.total++
      if (p.estado === 'APROBADA') acc.aprobadas++
      map.set(key, acc)
    }
    return Array.from(map.entries())
      .map(([profesor, acc]) => ({
        profesor,
        cumplimiento: acc.total === 0 ? 0 : Math.round((acc.aprobadas / acc.total) * 100),
      }))
      .sort((a, b) => b.cumplimiento - a.cumplimiento)
      .slice(0, 10)
  }, [consolidado])

  const datosPorMateria = useMemo(() => {
    const map = new Map<string, { total: number; aprobadas: number }>()
    for (const p of consolidado) {
      const key = p.materiaNombre || p.materiaId
      const acc = map.get(key) || { total: 0, aprobadas: 0 }
      acc.total++
      if (p.estado === 'APROBADA') acc.aprobadas++
      map.set(key, acc)
    }
    return Array.from(map.entries())
      .map(([materia, acc]) => ({
        materia: materia.length > 25 ? `${materia.substring(0, 25)}...` : materia,
        cumplimiento: acc.total === 0 ? 0 : Math.round((acc.aprobadas / acc.total) * 100),
      }))
      .sort((a, b) => b.cumplimiento - a.cumplimiento)
      .slice(0, 10)
  }, [consolidado])

  return (
    <Box sx={{ p: { xs: 2, sm: 3 }, maxWidth: 1400, mx: 'auto' }}>
      <EncabezadoPagina titulo="Dashboard y reportes" descripcion="Vista consolidada con gráficas de cumplimiento por profesor, materia y parcial." icono={<BarChart3 size={26} />} />

      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ alignItems: { sm: 'center' } }}>
            <FormControl size="small" sx={{ minWidth: 220 }}>
              <InputLabel>Periodo (ciclo)</InputLabel>
              <Select value={periodoId} label="Periodo (ciclo)" onChange={(e) => setPeriodoId(e.target.value)}>
                <MenuItem value="">Todos los periodos</MenuItem>
                {periodos.map((pe) => (
                  <MenuItem key={pe.id} value={pe.id}>
                    {pe.nombre}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <Button onClick={() => void cargar()} disabled={cargando} variant="outlined" startIcon={<RefreshCw size={16} />}>
              {cargando ? 'Actualizando...' : 'Actualizar'}
            </Button>
          </Stack>
        </CardContent>
      </Card>

      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(4, 1fr)' }, mb: 3 }}>
        <TarjetaEstadistica etiqueta="Total planeaciones" valor={repPlaneaciones?.total ?? 0} icono={<ClipboardCheck size={22} />} variedad="primario" />
        <TarjetaEstadistica etiqueta="% Cumplimiento" valor={repPlaneaciones?.porcentajeCumplimiento ?? 0} icono={<TrendingUp size={22} />} variedad="exito" />
        <TarjetaEstadistica etiqueta="Avances totales" valor={repAvances?.totalAvances ?? 0} icono={<History size={22} />} variedad="neutro" />
        <TarjetaEstadistica etiqueta="Cursos acreditados" valor={repCursos?.totalCursos ?? 0} icono={<Users size={22} />} variedad="primario" />
      </Box>

      <Box sx={{ display: 'grid', gap: 2, gridTemplateColumns: { xs: '1fr', lg: '1fr 1fr' } }}>
        <Card>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 2 }}>
              Cumplimiento por profesor (top 10)
            </Typography>
            <Box sx={{ height: 360 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={datosPorProfesor} layout="vertical">
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis type="number" domain={[0, 100]} />
                  <YAxis dataKey="profesor" type="category" width={140} />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="cumplimiento" fill={theme.palette.primary.main} name="Cumplimiento %" />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </CardContent>
        </Card>

        <Card>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 2 }}>
              Cumplimiento por materia (top 10)
            </Typography>
            <Box sx={{ height: 360 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={datosPorMateria}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="materia" angle={-25} textAnchor="end" height={70} />
                  <YAxis domain={[0, 100]} />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="cumplimiento" fill={theme.palette.success.main} name="Cumplimiento %" />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </CardContent>
        </Card>

        <Card sx={{ gridColumn: { xs: '1 / -1' } }}>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 2 }}>
              Cumplimiento por parcial
            </Typography>
            <Box sx={{ height: 380 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={repAvances?.parciales ?? []}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="parcialNombre" angle={-20} textAnchor="end" height={70} />
                  <YAxis domain={[0, 100]} />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="cumplidos" fill={theme.palette.success.main} name="Cumplidos" />
                  <Bar dataKey="total" fill={alpha(theme.palette.text.secondary, 0.25)} name="Total" />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </CardContent>
        </Card>

        <Card sx={{ gridColumn: { xs: '1 / -1' } }}>
          <CardContent>
            <Typography variant="h6" sx={{ mb: 2 }}>
              Cursos acreditados por periodo (institucional)
            </Typography>
            <Box sx={{ height: 360 }}>
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={repCursos?.periodos ?? []}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="periodoNombre" angle={-20} textAnchor="end" height={70} />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Bar dataKey="cursosAcreditados" fill={theme.palette.primary.main} name="Cursos acreditados" />
                  <Bar dataKey="horasAcreditadas" fill={theme.palette.warning.main} name="Horas acreditadas" />
                </BarChart>
              </ResponsiveContainer>
            </Box>
          </CardContent>
        </Card>
      </Box>
    </Box>
  )
}
