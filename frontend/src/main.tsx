import '@fontsource-variable/inter'
import '@fontsource-variable/space-grotesk'
import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
import { LocalizationProvider } from '@mui/x-date-pickers/LocalizationProvider'
import dayjs from 'dayjs'
import 'dayjs/locale/es'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.tsx'
import { TemaApp } from './theme/TemaApp'
import './index.css'

dayjs.locale('es')

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <TemaApp>
      <LocalizationProvider dateAdapter={AdapterDayjs} adapterLocale="es">
        <App />
      </LocalizationProvider>
    </TemaApp>
  </StrictMode>,
)