# Guía rápida: Control de acceso estricto a datos personales (S3-09)

## 1) Objetivo

Todas las rutas que exponen datos personales (usuarios, planeaciones, avances,
evidencias, archivos, reportes y calendario) quedan protegidas por rol y, cuando
corresponde, por propiedad del recurso. La autorización no depende solo de estar
autenticado: cada endpoint exige un rol concreto y los datos de un docente solo
los modifica ese docente.

La política está centralizada en `PoliticaAccesoDatos` y se aplica en dos capas:

1. **Reglas de URL** en `SecurityConfig` (primera barrera).
2. **`@PreAuthorize`** en los controladores y validaciones de propiedad en los
   servicios (segunda barrera).

## 2) Roles

| Rol | Alcance |
| --- | --- |
| `DOCENTE` | Solo sus propios datos: planeaciones, avances, evidencias, archivos y calendario. |
| `COORDINADOR` | Consulta y revisa planeaciones/evidencias de los docentes; no modifica datos ajenos. |
| `DIRECCION` | Consulta datos personales y administra usuarios. No modifica planeaciones/evidencias ajenas. |
| `ADMIN` | Administra configuración de seguridad, cifrado, retención y la matriz RBAC. **No** consulta datos personales. |
| `JEFE_DE_CARRERA` | Rol reservado para el calendario; no se siembra en la base todavía. |

> La migración `V17__rol_admin.sql` agrega el rol `ADMIN`, que ya exigían los
> endpoints de retención (S3-07) y cifrado (S3-08).

## 3) Matriz de acceso

| Método | Ruta | Roles permitidos | Regla adicional |
| --- | --- | --- | --- |
| `POST` | `/api/auth/registro`, `/api/auth/login` | Público | — |
| `GET` | `/api/auth/me` | Autenticado | Solo devuelve el perfil propio |
| `GET` | `/api/usuarios`, `/api/usuarios/{id}`, `/api/roles` | `COORDINADOR`, `DIRECCION` | — |
| `POST` | `/api/usuarios` | `DIRECCION` | — |
| `PUT` | `/api/usuarios/{id}` | `DIRECCION` | — |
| `DELETE` | `/api/usuarios/{id}` | `DIRECCION` | No puede darse de baja a sí mismo |
| `POST` | `/api/usuarios/{id}/reactivar` | `DIRECCION` | — |
| `GET` | `/api/reportes/**` | `COORDINADOR`, `DIRECCION` | — |
| `POST` | `/api/planeaciones/{id}/revisiones` | `COORDINADOR` | — |
| `POST` | `/api/evidencias/{id}/revisiones` | `COORDINADOR` | — |
| `GET` | `/api/planeaciones/**`, `/api/evidencias/**` | `DOCENTE`, `COORDINADOR`, `DIRECCION` | El docente solo ve lo propio |
| `POST/PUT/DELETE` | `/api/planeaciones/**`, `/api/evidencias/**` | `DOCENTE` | Solo el propietario |
| `GET` | `/api/planeaciones/{id}/avances/**`, `/api/planeaciones/{id}/archivos/**` | `DOCENTE`, `COORDINADOR`, `DIRECCION` | El docente solo ve lo propio |
| `POST/PUT/DELETE` | `/api/planeaciones/{id}/avances/**`, `/api/planeaciones/{id}/archivos/**` | `DOCENTE` | Solo el propietario |
| `GET` | `/api/calendario/**` | `DOCENTE`, `COORDINADOR`, `JEFE_DE_CARRERA`, `ADMIN` | Solo la conexión/eventos del usuario autenticado |
| `GET` | `/api/periodos/**`, `/api/materias/**`, `/api/parciales/**` | `DOCENTE`, `COORDINADOR`, `DIRECCION` | Catálogos académicos |
| `POST/PUT/DELETE` | `/api/periodos/**`, `/api/materias/**`, `/api/parciales/**` | `COORDINADOR`, `DIRECCION` | Catálogos académicos |
| `GET/POST` | `/api/retencion/**`, `/api/cifrado/**`, `/api/seguridad/**` | `ADMIN` | Administración del sistema |

La matriz también está disponible en tiempo de ejecución:

- `GET /api/seguridad/rbac` (solo `ADMIN`) devuelve la política y sus reglas.

## 4) Comprobación manual

### 4.1 Obtener tokens

```powershell
$base = "http://localhost:8080"
$docente = Invoke-RestMethod -Method Post "$base/api/auth/login" -ContentType "application/json" `
  -Body '{"email":"docente@escuela.mx","password":"TuPassword123"}'
$tokenDocente = $docente.token
```

Repite el login con un usuario de cada rol (`COORDINADOR`, `DIRECCION`, `ADMIN`).
El usuario `ADMIN` se puede crear desde `POST /api/usuarios` autenticado como
`DIRECCION`, usando `"rol":"ADMIN"`.

### 4.2 Datos personales de usuarios

```powershell
# Sin token -> 401
Invoke-WebRequest "$base/api/usuarios" -SkipHttpErrorCheck | Select-Object StatusCode

# DOCENTE -> 403
Invoke-WebRequest "$base/api/usuarios" -Headers @{ Authorization = "Bearer $tokenDocente" } `
  -SkipHttpErrorCheck | Select-Object StatusCode

# COORDINADOR y DIRECCION -> 200
Invoke-WebRequest "$base/api/usuarios" -Headers @{ Authorization = "Bearer $tokenCoordinador" } `
  -SkipHttpErrorCheck | Select-Object StatusCode

# ADMIN no consulta datos personales -> 403
Invoke-WebRequest "$base/api/usuarios" -Headers @{ Authorization = "Bearer $tokenAdmin" } `
  -SkipHttpErrorCheck | Select-Object StatusCode
```

### 4.3 Alta de usuarios

```powershell
# COORDINADOR -> 403
# DIRECCION -> 201
$body = '{"nombre":"Nuevo Docente","email":"nuevo@escuela.mx","password":"Password123","rol":"DOCENTE"}'
Invoke-WebRequest -Method Post "$base/api/usuarios" `
  -Headers @{ Authorization = "Bearer $tokenDireccion" } `
  -ContentType "application/json" -Body $body -SkipHttpErrorCheck | Select-Object StatusCode
```

### 4.4 Propiedad de una planeación

```powershell
# El docente B intenta leer una planeación del docente A -> 403
Invoke-WebRequest "$base/api/planeaciones/$idPlaneacionDocenteA" `
  -Headers @{ Authorization = "Bearer $tokenDocenteB" } -SkipHttpErrorCheck | Select-Object StatusCode

# COORDINADOR puede consultarla para revisión -> 200
Invoke-WebRequest "$base/api/planeaciones/$idPlaneacionDocenteA" `
  -Headers @{ Authorization = "Bearer $tokenCoordinador" } -SkipHttpErrorCheck | Select-Object StatusCode
```

### 4.5 Matriz RBAC y administración

```powershell
# DOCENTE -> 403
Invoke-WebRequest "$base/api/seguridad/rbac" -Headers @{ Authorization = "Bearer $tokenDocente" } `
  -SkipHttpErrorCheck | Select-Object StatusCode

# ADMIN -> 200
Invoke-WebRequest "$base/api/seguridad/rbac" -Headers @{ Authorization = "Bearer $tokenAdmin" } `
  -SkipHttpErrorCheck | Select-Object StatusCode

# Retención y cifrado: DIRECCION -> 403, ADMIN -> 200
Invoke-WebRequest "$base/api/retencion/politica" -Headers @{ Authorization = "Bearer $tokenAdmin" } `
  -SkipHttpErrorCheck | Select-Object StatusCode
Invoke-WebRequest "$base/api/cifrado/estado" -Headers @{ Authorization = "Bearer $tokenAdmin" } `
  -SkipHttpErrorCheck | Select-Object StatusCode
```

En Swagger/Postman/Insomnia puedes repetir los mismos casos usando el botón
**Authorize** con el token `Bearer`.

## 5) Notas

- Un `403` significa que el rol no está autorizado; un `401` significa que falta
  el token o es inválido.
- `ADMIN` es un rol técnico: administra seguridad y privacidad, pero no lee datos
  personales. Si en el futuro se requiere un superusuario con acceso a todo, debe
  documentarse y aprobarse como una excepción explícita.
- La verificación de propiedad se hace en los servicios (`PlaneacionService`,
  `EvidenciaService`, `AvanceService`, `ArchivoService`, `RevisionService` y
  `RevisionEvidenciaService`), no solo en el controlador.
- Siguientes pasos de privacidad: derechos ARCO (S3-10), bitácora de auditoría
  (S3-11) y consentimiento para transferencia a terceros (S3-12).
