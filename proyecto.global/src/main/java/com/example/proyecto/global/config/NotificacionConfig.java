package com.example.proyecto.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(NotificacionProperties.class)
public class NotificacionConfig {
}
