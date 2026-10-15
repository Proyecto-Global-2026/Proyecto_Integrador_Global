## Comentario del reviewer

```markdown
## Revisión — S2-11: Dashboard y reportes

**Rama:** `feature/S2-10-Reportes-Institucionales` → `develop`
**Reviewer:** @manuellopez-dev
**Estado:** ✅ Aprobado

## Verificado

### Vista consolidada
- [x] Filtro por periodo (con "Todos los periodos")
- [x] Tarjetas: total planeaciones, % cumplimiento, avances totales, cursos acreditados
- [x] Dashboard accesible para COORDINADOR y DIRECCIÓN

### Gráficas
- [x] Cumplimiento por profesor (top 10) — % aprobadas/total
- [x] Cumplimiento por materia (top 10) — % aprobadas/total
- [x] Cumplimiento por parcial — desglose por estado
- [x] Cursos acreditados por periodo (institucional) — cursos y horas

### Integración
- [x] Consume endpoints `/api/reportes/planeaciones`, `/api/reportes/avances`, `/api/reportes/cursos`
- [x] Agregación por profesor/materia desde `/api/planeaciones`

### Calidad
- [x] `npm run lint` OK
- [x] `npm run build` OK

## Observaciones
- Sin bloqueantes.

## Resultado
Se aprueba el PR. Listo para merge a develop.
```
