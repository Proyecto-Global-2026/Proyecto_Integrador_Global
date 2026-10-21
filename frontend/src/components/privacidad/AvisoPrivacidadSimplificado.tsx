import { Box, Link, Typography } from '@mui/material'
import { Link as RouterLink } from 'react-router-dom'

export function AvisoPrivacidadSimplificado() {
  return (
    <Box
      sx={{
        mt: 2,
        p: 2,
        border: '1px solid',
        borderColor: 'divider',
        borderRadius: 1,
        bgcolor: 'background.paper',
      }}
    >
      <Typography variant="subtitle2" sx={{ fontWeight: 600, mb: 1 }}>
        Aviso de Privacidad Simplificado
      </Typography>
      <Typography variant="body2" sx={{ mb: 1 }}>
        <strong>Denominación del responsable:</strong> Universidad Tecnológica del Norte de Guanajuato (UTNG).
      </Typography>
      <Typography variant="body2" sx={{ mb: 1 }}>
        <strong>Finalidades del tratamiento:</strong> Registro, seguimiento, revisión y aprobación de las planeaciones
        didácticas; control y verificación de avances programáticos por parcial y cuatrimestre; recepción, acreditación
        y seguimiento de evidencias de capacitación e historial académico docente; generación de reportes institucionales
        de cumplimiento para coordinaciones y direcciones de carrera; control de acceso, autenticación por roles
        (Docente, Coordinador, Dirección) y auditoría de cambios dentro de la plataforma.
      </Typography>
      <Typography variant="body2" sx={{ mb: 1 }}>
        <strong>De las transferencias:</strong> Sus datos personales no serán transferidos a terceros salvo a autoridades
        educativas o fiscalizadoras en cumplimiento de facultades legales expresas y para la evaluación de la calidad
        académica institucional.
      </Typography>
      <Typography variant="body2">
        Si requiere mayor información sobre el tratamiento de sus datos personales y la forma de ejercer sus derechos
        ARCO, puede consultar el{' '}
        <Link component={RouterLink} to="/aviso-privacidad" target="_blank" rel="noopener noreferrer">
          Aviso de Privacidad Integral
        </Link>
        .
      </Typography>
      <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 1 }}>
        Última actualización: 21 de octubre de 2026
      </Typography>
    </Box>
  )
}
