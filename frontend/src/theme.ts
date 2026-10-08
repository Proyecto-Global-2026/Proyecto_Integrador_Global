import { createTheme } from '@mui/material/styles'

export const theme = createTheme({
  palette: {
    mode: 'light',
    primary: { main: '#4f46e5', dark: '#3730a3', light: '#818cf8' },
    secondary: { main: '#0ea5e9' },
    background: { default: '#f8fafc', paper: '#ffffff' },
    success: { main: '#10b981' },
  },
  shape: { borderRadius: 14 },
  typography: {
    fontFamily: "'Inter', system-ui, 'Segoe UI', Roboto, sans-serif",
    button: { textTransform: 'none', fontWeight: 600 },
    h5: { fontWeight: 700, letterSpacing: '-0.02em' },
    h6: { fontWeight: 700, letterSpacing: '-0.01em' },
  },
  components: {
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          minHeight: '100vh',
          backgroundImage:
            'radial-gradient(1100px 520px at 8% -12%, #e0e7ff 0%, transparent 55%), radial-gradient(900px 520px at 112% 112%, #cffafe 0%, transparent 52%)',
          backgroundAttachment: 'fixed',
        },
      },
    },
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: 10,
          paddingInline: 20,
          transition: 'all 0.2s ease',
          '&.MuiButton-containedPrimary': {
            backgroundImage: 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)',
            boxShadow: '0 10px 22px -12px rgba(99, 102, 241, 0.9)',
            '&:hover': {
              backgroundImage: 'linear-gradient(135deg, #4f46e5 0%, #7c3aed 100%)',
              boxShadow: '0 14px 26px -12px rgba(99, 102, 241, 1)',
              transform: 'translateY(-1px)',
            },
          },
        },
        outlined: { borderColor: '#e2e8f0', color: '#334155' },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: 20,
          border: '1px solid #eef2f7',
          boxShadow: '0 20px 45px -28px rgba(15, 23, 42, 0.35)',
        },
      },
    },
    MuiOutlinedInput: {
      styleOverrides: {
        root: { borderRadius: 10, backgroundColor: '#ffffff' },
      },
    },
    MuiAlert: {
      styleOverrides: { root: { borderRadius: 12, fontWeight: 500 } },
    },
    MuiChip: {
      styleOverrides: { root: { borderRadius: 8, fontWeight: 600 } },
    },
    MuiDivider: {
      styleOverrides: { root: { borderColor: '#eef2f7' } },
    },
  },
})
