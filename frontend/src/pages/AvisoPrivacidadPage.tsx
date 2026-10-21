import Box from '@mui/material/Box'
import Container from '@mui/material/Container'
import Divider from '@mui/material/Divider'
import Paper from '@mui/material/Paper'
import Typography from '@mui/material/Typography'
import { ShieldCheck } from 'lucide-react'

export function AvisoPrivacidadPage() {
  return (
    <Container maxWidth="md" sx={{ py: 4 }}>
      <Paper sx={{ p: { xs: 2, sm: 4 } }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mb: 2 }}>
          <ShieldCheck size={26} />
          <Typography variant="h5" sx={{ fontWeight: 600 }}>
            Aviso de Privacidad Integral
          </Typography>
        </Box>

        <Typography variant="h6" sx={{ mt: 2, mb: 1 }}>
          Denominación del responsable
        </Typography>
        <Typography variant="body2" sx={{ mb: 2 }}>
          La Universidad Tecnológica del Norte de Guanajuato (UTNG), con domicilio en Avenida Educación Tecnológica número
          34, Fraccionamiento Universidad, Código Postal 37800, en la Ciudad de Dolores Hidalgo Cuna de la Independencia
          Nacional, Guanajuato, es la responsable del tratamiento de los datos personales que nos proporcione a través del
          Sistema de seguimiento de planeación didáctica y capacitación docente, los cuales serán protegidos conforme a lo
          dispuesto por la Ley de Protección de Datos Personales en Posesión de Sujetos Obligados para el Estado de
          Guanajuato, la Ley de Transparencia y Acceso a la Información Pública para el Estado de Guanajuato, y demás
          normatividad aplicable.
        </Typography>

        <Typography variant="h6" sx={{ mt: 3, mb: 1 }}>
          Finalidades del Tratamiento
        </Typography>
        <Typography variant="body2" sx={{ mb: 2 }}>
          Los datos personales recabados en este sistema serán utilizados para las siguientes actividades institucionales:
        </Typography>
        <Box component="ol" sx={{ pl: 3, mt: 0, mb: 2 }}>
          <li>Gestión y control de la planeación didáctica: Registro, revisión, aprobación y seguimiento del cumplimiento de los planes de asignatura presentados por el personal docente.</li>
          <li>Seguimiento del avance programático: Control y validación del avance real de contenidos por parcial y por cuatrimestre.</li>
          <li>Registro y validación de capacitación docente: Recepción, revisión y acreditación de constancias, diplomas y evidencias de cursos de formación y actualización profesional.</li>
          <li>Generación de reportes institucionales: Consolidación de indicadores de cumplimiento para la Coordinación Académica, Dirección de Carrera y Rectoría.</li>
          <li>Autenticación y control de acceso: Verificación de identidad y asignación de permisos según el rol asignado (Docente, Coordinador Académico, Dirección).</li>
          <li>Notificaciones y alertas del sistema: Envío de avisos automáticos sobre el estado de aprobación, correcciones pendientes o fechas límite de entrega de planeaciones y evidencias.</li>
          <li>Evaluación del desempeño académico y docente: Acreditación de estándares de calidad educativa e inspección por parte de organismos acreditadores e instancias gubernamentales.</li>
        </Box>

        <Typography variant="h6" sx={{ mt: 3, mb: 1 }}>
          Datos Personales Recabados
        </Typography>
        <Typography variant="body2" sx={{ mb: 2 }}>
          Para el cumplimiento de las finalidades descritas, el sistema recolecta y trata los siguientes datos personales:
        </Typography>
        <Box component="ul" sx={{ pl: 3, mt: 0, mb: 2 }}>
          <li><strong>Datos de identificación:</strong> Nombre(s) y apellido(s) completos, fotografía de perfil, firma digital o escaneada.</li>
          <li><strong>Datos laborales e institucionales:</strong> Número de empleado/matrícula, correo electrónico institucional, cargo o función (Docente, Coordinador, Director), adscripción académica (área o carrera) y materias asignadas.</li>
          <li><strong>Datos académicos y profesionales:</strong> Grado académico, títulos, cédula profesional, historial de capacitaciones, certificaciones y constancias académicas.</li>
          <li><strong>Datos de uso e interacción técnica:</strong> Registros de acceso (logs de auditoría), dirección IP, marcas de tiempo de subida de archivos y cambios realizados en el sistema.</li>
        </Box>
        <Typography variant="body2" sx={{ mb: 2 }}>
          <strong>Nota sobre Datos Sensibles:</strong> El Sistema de seguimiento de planeación didáctica y capacitación docente no recaba ni trata datos personales sensibles (origen étnico, estado de salud, datos biométricos, entre otros).
        </Typography>

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" sx={{ mb: 1 }}>
          Fundamento Legal
        </Typography>
        <Typography variant="body2" sx={{ mb: 2 }}>
          El tratamiento de sus datos personales se fundamenta en los artículos 48 fracciones IV, VI y IX, 83, 85, 99 y 137
          de la Ley de Transparencia y Acceso a la Información Pública para el Estado de Guanajuato; artículos 78, 96, 97,
          98, 99, 100, 101, 117, 119 fracción II, y 179 de la Ley de Protección de Datos Personales en Posesión de Sujetos
          Obligados para el Estado de Guanajuato; y el artículo 1 del Decreto Gubernativo número 241, publicado en el
          Periódico Oficial del Gobierno del Estado de Guanajuato.
        </Typography>

        <Typography variant="h6" sx={{ mb: 1 }}>
          Transferencia de Datos Personales
        </Typography>
        <Typography variant="body2" sx={{ mb: 2 }}>
          Se le informa que sus datos personales no serán transferidos a terceros ni utilizados para fines distintos a los
          aquí señalados, salvo aquellas transmisiones que sean necesarias para atender requerimientos de información de
          autoridades gubernamentales competentes (Estatales o Federales), órganos auditoros o instancias acreditadoras
          de la calidad educativa, en el ejercicio de sus funciones legales.
        </Typography>

        <Typography variant="h6" sx={{ mb: 1 }}>
          Derechos ARCO (Acceso, Rectificación, Cancelación y Oposición)
        </Typography>
        <Typography variant="body2" sx={{ mb: 2 }}>
          Usted tiene derecho a conocer qué datos personales tenemos de usted, para qué los utilizamos y las condiciones del
          uso que les damos (Acceso); solicitar la corrección de su información personal en caso de que esté
          desactualizada, sea inexacta o incompleta (Rectificación); que la eliminemos de nuestros registros o bases de
          datos cuando considere que la misma no está siendo utilizada adecuadamente (Cancelación); así como oponerse al
          uso de sus datos personales para fines específicos (Oposición).
        </Typography>
        <Typography variant="body2" sx={{ mb: 2 }}>
          Para el ejercicio de cualquiera de los derechos ARCO, podrá presentar su solicitud por escrito ante la Unidad de
          Transparencia del Poder Ejecutivo del Estado de Guanajuato, o bien realizarla a través de la Plataforma Nacional
          de Transparencia:
        </Typography>
        <Box component="ul" sx={{ pl: 3, mt: 0, mb: 2 }}>
          <li>Sitio Web: http://www.plataformadetransparencia.org.mx</li>
          <li>Correo electrónico: unidadtransparencia@guanajuato.gob.mx</li>
          <li>Domicilio: Calle San Sebastián #78, Zona Centro, Guanajuato, Gto., C.P. 36000.</li>
          <li>Teléfono: (473) 688-0470</li>
        </Box>

        <Typography variant="h6" sx={{ mb: 1 }}>
          Cambios al Aviso de Privacidad
        </Typography>
        <Typography variant="body2">
          El presente aviso de privacidad puede sufrir modificaciones, cambios o actualizaciones derivadas de nuevos
          requerimientos legales o de mejoras continuas en la plataforma web. Cualquier modificación será notificada a
          través del portal institucional de la Universidad en www.utng.edu.mx o mediante la interfaz del propio
          sistema.
        </Typography>

        <Typography variant="caption" color="text.secondary" sx={{ display: 'block', mt: 3 }}>
          Última actualización: 21 de octubre de 2026.
        </Typography>
      </Paper>
    </Container>
  )
}
