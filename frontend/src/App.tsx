import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from './auth/AuthContext'
import { HomePage } from './pages/HomePage'
import { LoginPage } from './pages/LoginPage'
import { OAuth2CallbackPage } from './pages/OAuth2CallbackPage'
import { UsuariosPage } from './pages/UsuariosPage'
import { MateriasPage } from './pages/MateriasPage'
import { PeriodosPage } from './pages/PeriodosPage'
import { ParcialesPage } from './pages/ParcialesPage'
import { ProtectedRoute } from './routes/ProtectedRoute'
import { RoleGuard } from './routes/RoleGuard'
import { LayoutPrivado } from './routes/LayoutPrivado'
import { Toaster } from 'sileo'
import 'sileo/styles.css'
import { useTema } from './theme/contextoTema'

function App() {
  const { modo } = useTema()
  return (
    <AuthProvider>
      <BrowserRouter>
        <Toaster position="bottom-right" theme={modo} />
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/oauth2/callback" element={<OAuth2CallbackPage />} />
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <LayoutPrivado>
                  <HomePage />
                </LayoutPrivado>
              </ProtectedRoute>
            }
          />
          <Route
            path="/usuarios"
            element={
              <ProtectedRoute>
                <RoleGuard roles={['COORDINADOR', 'DIRECCION']}>
                  <LayoutPrivado>
                    <UsuariosPage />
                  </LayoutPrivado>
                </RoleGuard>
              </ProtectedRoute>
            }
          />
          <Route
            path="/materias"
            element={
              <ProtectedRoute>
                <RoleGuard roles={['COORDINADOR', 'DIRECCION']}>
                  <LayoutPrivado>
                    <MateriasPage />
                  </LayoutPrivado>
                </RoleGuard>
              </ProtectedRoute>
            }
          />
          <Route
            path="/periodos"
            element={
              <ProtectedRoute>
                <RoleGuard roles={['COORDINADOR', 'DIRECCION']}>
                  <LayoutPrivado>
                    <PeriodosPage />
                  </LayoutPrivado>
                </RoleGuard>
              </ProtectedRoute>
            }
          />
          <Route
            path="/parciales"
            element={
              <ProtectedRoute>
                <RoleGuard roles={['COORDINADOR', 'DIRECCION']}>
                  <LayoutPrivado>
                    <ParcialesPage />
                  </LayoutPrivado>
                </RoleGuard>
              </ProtectedRoute>
            }
          />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}

export default App
