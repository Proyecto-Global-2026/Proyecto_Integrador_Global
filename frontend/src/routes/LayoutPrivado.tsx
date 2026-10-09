import type { ReactNode } from 'react'
import { MenuPrincipal } from '../components/MenuPrincipal'

export function LayoutPrivado({ children }: { children: ReactNode }) {
  return (
    <>
      <MenuPrincipal />
      {children}
    </>
  )
}
