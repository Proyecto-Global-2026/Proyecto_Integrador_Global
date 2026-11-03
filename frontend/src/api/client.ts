import axios from 'axios'
import { clearToken, getToken } from '../auth/token'
import { validarUrlApi } from '../utils/seguridad'

const API_URL = validarUrlApi('https://proyecto-integrador-global.onrender.com')

export const api = axios.create({
  baseURL: API_URL,
})

export const API_BASE_URL = API_URL

api.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

api.interceptors.response.use(
  (respuesta) => respuesta,
  (error: unknown) => {
    if (axios.isAxiosError(error)) {
      const url = error.config?.url ?? ''
      const esLogin = url.endsWith('/api/auth/login')
      if (error.response?.status === 401 && !esLogin) {
        clearToken()
        window.dispatchEvent(new Event('auth:expirada'))
      }
    }
    return Promise.reject(error)
  },
)
