import '@fontsource-variable/inter'
import '@fontsource-variable/space-grotesk'
import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import App from './App.tsx'
import { TemaApp } from './theme/TemaApp'
import './index.css'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <TemaApp>
      <App />
    </TemaApp>
  </StrictMode>,
)