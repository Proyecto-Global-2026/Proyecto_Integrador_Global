# Guía rápida: Cifrado de datos en reposo (S3-08)

## 1) Configurar clave maestra
Añade a tu `.env` (no subir al repositorio):

```env
JASYPT_ENCRYPTOR_PASSWORD=tu-clave-maestra-muy-segura
```

## 2) Cifrar un valor sensible
Con Jasypt puedes generar valores cifrados. Ejemplo con Maven:

```bash
mvn jasypt:encrypt-value -Djasypt.encryptor.password=$JASYPT_ENCRYPTOR_PASSWORD -Djasypt.plugin.value="MI_SECRETO"
```

El resultado lo envuelves con `ENC(...)`:

```properties
app.notificaciones.sendgrid-api-key=ENC(AQAA...cifrado...)
GOOGLE_CALENDAR_CLIENT_SECRET=ENC(AQAA...cifrado...)
JWT_SECRET=ENC(AQAA...cifrado...)
```

## 3) Comprobar en la app
Con la app arrancada y autenticado como ADMIN:

- `GET /api/cifrado/estado` -> muestra que el cifrado está habilitado y el algoritmo usado.

## Notas
- Los tokens OAuth2 de terceros (Google Calendar) y credenciales se tratan con cifrado en reposo.
- Nunca commitees `JASYPT_ENCRYPTOR_PASSWORD`. Debe mantenerse en variables de entorno o en un gestor de secretos.
- En desarrollo puedes dejar la clave vacía solo para arrancar sin cifrado, pero en producción debe estar presente.
