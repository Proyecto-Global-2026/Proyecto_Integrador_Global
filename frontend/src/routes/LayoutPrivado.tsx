import type { ReactNode } from 'react'
import { AppLayout } from '../layouts/AppLayout'

export function LayoutPrivado({ children }: { children: ReactNode }) {
  return <AppLayout>{children}</AppLayout>
}