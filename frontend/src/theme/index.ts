import { createTheme } from '@mui/material/styles'

export type Modo = 'light' | 'dark'

export interface Token {
  fondo: string
  superficie: string
  elevada: string
  borde: string
  texto: string
  textoSecundario: string
  primario: string
  secundario: string
  exito: string
  error: string
  advertencia: string
}

export const TOKENS: Record<Modo, Token> = {
  dark: {
    fondo: '#0A1020',
    superficie: '#0F1A33',
    elevada: '#14213F',
    borde: 'rgba(255, 255, 255, 0.08)',
    texto: '#E6EDF7',
    textoSecundario: '#9FB0CC',
    primario: '#22D3EE',
    secundario: '#FBBF24',
    exito: '#34D399',
    error: '#F87171',
    advertencia: '#FB923C',
  },
  light: {
    fondo: '#F3F6FC',
    superficie: '#FFFFFF',
    elevada: '#FFFFFF',
    borde: 'rgba(15, 23, 42, 0.1)',
    texto: '#0F172A',
    textoSecundario: '#475569',
    primario: '#0891B2',
    secundario: '#B45309',
    exito: '#059669',
    error: '#DC2626',
    advertencia: '#EA580C',
  },
}

export const FUENTE_TITULO = "'Space Grotesk Variable', 'Inter Variable', system-ui, sans-serif"
export const FUENTE_CUERPO = "'Inter Variable', system-ui, 'Segoe UI', Roboto, sans-serif"

export function glow(color: string, intensidad = '40') {
  return `0 0 24px -6px ${color}${intensidad}`
}

export const paletaGraficas = [
  '#22D3EE',
  '#FBBF24',
  '#34D399',
  '#A78BFA',
  '#F87171',
  '#FB923C',
  '#38BDF8',
  '#F472B6',
]

export const COLORES_ESTADO = {
  pendiente: '#FBBF24',
  enRevision: '#22D3EE',
  aprobada: '#34D399',
  cumplido: '#34D399',
  ajustes: '#FB923C',
  parcial: '#FB923C',
  rechazada: '#F87171',
  noCumplido: '#F87171',
} as const

export function crearTema(modo: Modo = 'dark') {
  const t = TOKENS[modo]

  return createTheme({
    palette: {
      mode: modo,
      primary: { main: t.primario, contrastText: '#04120F' },
      secondary: { main: t.secundario, contrastText: '#1A1204' },
      success: { main: t.exito },
      error: { main: t.error },
      warning: { main: t.advertencia },
      info: { main: t.primario },
      background: { default: t.fondo, paper: t.superficie },
      text: { primary: t.texto, secondary: t.textoSecundario },
      divider: t.borde,
    },
    shape: { borderRadius: 14 },
    typography: {
      fontFamily: FUENTE_CUERPO,
      h1: { fontFamily: FUENTE_TITULO, fontWeight: 750, letterSpacing: '-0.03em' },
      h2: { fontFamily: FUENTE_TITULO, fontWeight: 750, letterSpacing: '-0.025em' },
      h3: { fontFamily: FUENTE_TITULO, fontWeight: 700, letterSpacing: '-0.02em' },
      h4: { fontFamily: FUENTE_TITULO, fontWeight: 700, letterSpacing: '-0.02em' },
      h5: { fontFamily: FUENTE_TITULO, fontWeight: 700, letterSpacing: '-0.015em' },
      h6: { fontFamily: FUENTE_TITULO, fontWeight: 650, letterSpacing: '-0.01em' },
      subtitle1: { fontFamily: FUENTE_TITULO, fontWeight: 600 },
      subtitle2: { fontFamily: FUENTE_TITULO, fontWeight: 600 },
      button: { textTransform: 'none', fontWeight: 600 },
      overline: { letterSpacing: '0.08em', fontWeight: 600 },
    },
    components: {
      MuiCssBaseline: {
        styleOverrides: {
          body: {
            minHeight: '100vh',
            backgroundImage:
              modo === 'dark'
                ? 'radial-gradient(1200px 600px at 85% -10%, rgba(34, 211, 238, 0.10) 0%, transparent 55%), radial-gradient(1000px 520px at -10% 110%, rgba(167, 139, 250, 0.09) 0%, transparent 55%)'
                : 'radial-gradient(1200px 600px at 85% -10%, rgba(34, 211, 238, 0.16) 0%, transparent 55%), radial-gradient(1000px 520px at -10% 110%, rgba(167, 139, 250, 0.12) 0%, transparent 55%)',
            backgroundAttachment: 'fixed',
            '&:focus-visible': {
              outline: `2px solid ${t.primario}`,
              outlineOffset: 2,
            },
          },
          '*:focus-visible': {
            outline: `2px solid ${t.primario}`,
            outlineOffset: 2,
            borderRadius: 4,
          },
          '@media (prefers-reduced-motion: reduce)': {
            '*': {
              animationDuration: '0.01ms !important',
              animationIterationCount: '1 !important',
              transitionDuration: '0.01ms !important',
            },
          },
        },
      },
      MuiButton: {
        styleOverrides: {
          root: {
            borderRadius: 12,
            fontWeight: 600,
            transition: 'all 0.18s ease',
            '&:active': { transform: 'translateY(1px) scale(0.99)' },
            '&:disabled': { opacity: 0.55 },
          },
          text: {
            '&:hover': { backgroundColor: `${t.primario}14` },
          },
          outlined: { borderColor: t.borde },
          contained: {
            boxShadow: 'none',
            '&.MuiButton-colorPrimary': {
              backgroundImage: `linear-gradient(135deg, ${t.primario}, ${t.primario}dd)`,
              color: modo === 'dark' ? '#041318' : '#FFFFFF',
              boxShadow: glow(t.primario, '4D'),
              '&:hover': {
                backgroundImage: `linear-gradient(135deg, ${t.primario}, ${t.primario}b3)`,
                boxShadow: glow(t.primario, '80'),
              },
            },
            '&.MuiButton-colorSecondary': {
              backgroundImage: `linear-gradient(135deg, ${t.secundario}, ${t.secundario}dd)`,
              color: modo === 'dark' ? '#1A1204' : '#FFFFFF',
              boxShadow: glow(t.secundario, '40'),
              '&:hover': { boxShadow: glow(t.secundario, '75') },
            },
            '&.MuiButton-colorSuccess': {
              boxShadow: glow(t.exito, '40'),
              '&:hover': { boxShadow: glow(t.exito, '70') },
            },
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            borderRadius: 18,
            backgroundImage: 'none',
            backgroundColor: t.superficie,
            border: `1px solid ${t.borde}`,
            boxShadow: modo === 'dark' ? '0 18px 40px -30px rgba(0, 0, 0, 0.8)' : '0 18px 40px -32px rgba(15, 23, 42, 0.35)',
            backdropFilter: 'blur(10px)',
          },
        },
      },
      MuiPaper: {
        styleOverrides: {
          root: {
            backgroundImage: 'none',
            backgroundColor: t.superficie,
          },
        },
      },
      MuiDialog: {
        styleOverrides: {
          paper: {
            borderRadius: 20,
            backgroundImage: 'none',
            backgroundColor: t.elevada,
            border: `1px solid ${t.borde}`,
            boxShadow: '0 40px 90px -40px rgba(0, 0, 0, 0.85)',
          },
        },
      },
      MuiDialogTitle: {
        styleOverrides: { root: { fontFamily: FUENTE_TITULO, fontWeight: 700 } },
      },
      MuiTextField: {
        styleOverrides: {
          root: { '& .MuiInputBase-root': { borderRadius: 12, backgroundColor: 'transparent' } },
        },
      },
      MuiOutlinedInput: {
        styleOverrides: {
          root: {
            borderRadius: 12,
            transition: 'box-shadow 0.18s ease, border-color 0.18s ease',
            '&:hover .MuiOutlinedInput-notchedOutline': { borderColor: t.borde },
            '&.Mui-focused': {
              boxShadow: glow(t.primario, '33'),
              '& .MuiOutlinedInput-notchedOutline': { borderColor: t.primario, borderWidth: 1.5 },
            },
          },
          notchedOutline: { borderColor: t.borde },
        },
      },
      MuiChip: {
        styleOverrides: {
          root: { borderRadius: 9, fontWeight: 600, height: 28 },
          colorSuccess: { backgroundColor: `${t.exito}1C`, color: t.exito },
          colorError: { backgroundColor: `${t.error}1C`, color: t.error },
          colorWarning: { backgroundColor: `${t.advertencia}1C`, color: t.advertencia },
          colorInfo: { backgroundColor: `${t.primario}1C`, color: t.primario },
        },
      },
      MuiTooltip: {
        styleOverrides: {
          tooltip: {
            backgroundColor: t.texto,
            color: t.fondo,
            fontWeight: 600,
            fontSize: 12,
            borderRadius: 8,
            px: 1.5,
            py: 1,
          },
        },
      },
      MuiTabs: {
        styleOverrides: {
          root: {
            minHeight: 44,
            '& .MuiTab-root': { textTransform: 'none', fontWeight: 600, borderRadius: 10 },
            '& .MuiTabs-indicator': { height: 3, borderRadius: 3, backgroundImage: `linear-gradient(90deg, ${t.primario}, ${t.primario}00)` },
          },
        },
      },
      MuiPagination: {
        styleOverrides: {
          root: { '& .MuiPaginationItem-root': { borderRadius: 9 }, '& .Mui-selected': { boxShadow: glow(t.primario, '4D') } },
        },
      },
    },
  })
}