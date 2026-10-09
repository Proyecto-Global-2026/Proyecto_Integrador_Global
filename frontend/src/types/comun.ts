export interface Paginado<T> {
  content: T[]
  pagina: number
  tamanoPagina: number
  totalElementos: number
  totalPaginas: number
}
