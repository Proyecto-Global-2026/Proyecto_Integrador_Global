import { motion, useReducedMotion } from 'motion/react'
import type { ReactNode } from 'react'

export function AnimatedPage({ children }: { children: ReactNode }) {
  const reducir = useReducedMotion()
  return (
    <motion.div
      initial={{ opacity: 0, y: reducir ? 0 : 12 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.32, ease: 'easeOut' }}
    >
      {children}
    </motion.div>
  )
}