package com.example.proyecto.global.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.proyecto.global.config.RetencionProperties;
import com.example.proyecto.global.service.retencion.RetencionDatosService;

@Component
public class RetencionDatosJob {

	private static final Logger log = LoggerFactory.getLogger(RetencionDatosJob.class);

	private final RetencionDatosService retencionDatosService;
	private final RetencionProperties properties;

	public RetencionDatosJob(RetencionDatosService retencionDatosService, RetencionProperties properties) {
		this.retencionDatosService = retencionDatosService;
		this.properties = properties;
	}

	@Scheduled(cron = "${app.retencion.cron:0 0 2 * * *}")
	public void ejecutarRetencion() {
		if (!properties.habilitado()) {
			log.debug("Job de retención omitido (deshabilitado)");
			return;
		}
		log.info("Iniciando proceso automático de retención y minimización de datos (S3-07)");
		try {
			RetencionDatosService.ResultadoRetencion resultado = retencionDatosService.ejecutar();
			log.info("Proceso de retención completado: {}", resultado);
		} catch (Exception ex) {
			log.error("Error ejecutando proceso de retención de datos: {}", ex.getMessage(), ex);
		}
	}
}