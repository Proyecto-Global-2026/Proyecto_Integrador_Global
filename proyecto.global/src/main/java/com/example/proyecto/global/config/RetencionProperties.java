package com.example.proyecto.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuración de política de retención y minimización de datos personales (S3-07).
 */
@ConfigurationProperties(prefix = "app.retencion")
public record RetencionProperties(

		/** Habilita el proceso automático de retención. */
		@DefaultValue("false") boolean habilitado,

		/** Cron para ejecución del proceso automático. Por defecto diario a las 02:00 AM. */
		@DefaultValue("0 0 2 * * *") String cron,

		/** Días de inactividad tras los cuales se anonimiza un usuario. */
		@DefaultValue("730") int diasInactividadUsuario,

		/** Días tras los cuales se eliminan archivos huérfanos/obsoletos. */
		@DefaultValue("365") int diasEliminacionArchivos,

		/** Días tras los cuales se pueden limpiar revisiones históricas. */
		@DefaultValue("1095") int diasLimpiezaRevisiones,

		/** Dominio para emails anonimizados. */
		@DefaultValue("anon.local") String dominioAnonimizado) {
}