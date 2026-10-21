import Box from '@mui/material/Box'
import Container from '@mui/material/Container'
import Paper from '@mui/material/Paper'
import Typography from '@mui/material/Typography'
import { FileText } from 'lucide-react'

export function AvisoPrivacidadSimplificadoPage() {
  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Paper sx={{ p: { xs: 2, sm: 4 } }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
          <FileText size={26} />
          <Typography variant="h5" sx={{ fontWeight: 600 }}>
            Aviso de Privacidad Simplificado
          </Typography>
        </Box>

        <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>
          Denominación del responsable
        </Typography>
        <Typography variant="body2" sx={{ mb: 2 }}>
          Universidad Tecnológica del Norte de Guanajuato (UTNG).
        </Typography>

        <Typography variant="h6" sx={{ mt: 3, mb: 1 }}>
          Finalidades del tratamiento
        </Typography>
        <Typography variant="body2" sx={{ mb: 2 }}>
          Sus datos personales recopilados en el Sistema de seguimiento de planeación didáctica y capacitación docente
          serán tratados para las siguientes finalidades:
        </Typography>
        <Box component="ul" sx={{ pl: 3, mt: 0, mb: 2 }}>
          <li>Registro, seguimiento, revisión y aprobación de las planeaciones didácticas de las asignaturas.</li>
          <li>Control y verificación de avances programáticos por parcial y cuatrimestre.</li>
          <li>Recepción, acreditación y seguimiento de evidencias de capacitación e historial académico docente.</li>
          <li>Generación de reportes institucionales de cumplimiento para coordinaciones y direcciones de carrera.</li>
          <li>Control de acceso, autenticación por roles (Docente, Coordinador, Dirección) y auditoría de cambios dentro de la plataforma.</li>
        </Box>

        <Typography variant="h6" sx={{ mt: 3, mb: 1 }}>
          De las transferencias
        </Typography>
        <Typography variant="body2" sx={{ mb: 2 }}>
          Se le informa que sus datos personales no serán transferidos a terceros salvo a autoridades educativas o
          fiscalizadoras en cumplimiento de facultades legales expresas y para la evaluación de la calidad académica
          institucional.
        </Typography>

        <Typography variant="h6" sx={{ mt: 3, mb: 1 }}>
          Consulta del aviso de privacidad integral
        </Typography>
        <Typography variant="body2">
          Si requiere mayor información sobre el tratamiento de sus datos personales y la forma de ejercer sus derechos
          ARCO, puede consultar el Aviso de Privacidad Integral en la página web institucional: www.utng.edu.mx.
        </Typography>

        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 3 }}>
          Última actualización: 21 de octubre de 2026
        </Typography>
      </Paper>
    </Container>
  )
}
