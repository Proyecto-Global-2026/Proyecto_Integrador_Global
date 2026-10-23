/**
 * Utilidades para preparar los datos que se envian al backend.
 *
 * Regla del proyecto: el cliente solo envia los campos que el formulario
 * capturo realmente. Los textos opcionales vacios se omiten y los numeros
 * invalidos nunca llegan como cadenas vacias o valores fuera de rango.
 */

/** Recorta un texto obligatorio antes de enviarlo. */
export function textoRequerido(valor: string): string {
  return valor.trim()
}

/** Recorta un texto opcional y lo omite cuando queda vacio. */
export function textoOpcional(valor?: string | null): string | undefined {
  const limpio = valor?.trim()
  return limpio ? limpio : undefined
}

/** Indica si el valor es un entero mayor a cero. */
export function esEnteroPositivo(valor: number | string): boolean {
  const numero = Number(valor)
  return Number.isInteger(numero) && numero > 0
}
