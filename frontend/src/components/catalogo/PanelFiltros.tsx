import Card from '@mui/material/Card'
import CardContent from '@mui/material/CardContent'
import IconButton from '@mui/material/IconButton'
import Stack from '@mui/material/Stack'
import Tooltip from '@mui/material/Tooltip'
import { RefreshCw } from 'lucide-react'
import type { ReactNode } from 'react'

interface Props {
  children: ReactNode
  onRecargar?: () => void
}

export function PanelFiltros({ children, onRecargar }: Props) {
  return (
    <Card sx={{ mb: 2.5 }}>
      <CardContent sx={{ p: 2, '&:last-child': { pb: 2 } }}>
        <Stack direction={{ xs: 'column', sm: 'row' }} spacing={2} sx={{ alignItems: { sm: 'center' } }}>
          {children}
          {onRecargar && (
            <Tooltip title="Recargar">
              <IconButton onClick={onRecargar} size="small" sx={{ alignSelf: { xs: 'flex-end', sm: 'center' } }}>
                <RefreshCw size={18} />
              </IconButton>
            </Tooltip>
          )}
        </Stack>
      </CardContent>
    </Card>
  )
}