<div align="center">

# 📋 Sistema de Seguimiento de Planeación Didáctica

### Proyecto Integrador Global · Desarrollo Web Integral

Aplicación web para que instituciones de nivel medio superior den seguimiento al cumplimiento de la planeación didáctica docente, el avance por parcial y las evidencias de capacitación.

[![React](https://img.shields.io/badge/React-18-61DAFB?logo=react&logoColor=black)](https://react.dev)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org)
[![Docker](https://img.shields.io/badge/Docker-ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com)
[![License](https://img.shields.io/badge/license-MIT-lightgrey)](#-licencia)

</div>

---

## 📑 Tabla de contenido

- [Descripción](#-descripción)
- [Stack tecnológico](#-stack-tecnológico)
- [Arquitectura](#-arquitectura)
- [Estructura del repositorio](#-estructura-del-repositorio)
- [Requisitos previos](#-requisitos-previos)
- [Instalación y configuración](#-instalación-y-configuración)
- [Variables de entorno](#-variables-de-entorno)
- [Ejecución con Docker](#-ejecución-con-docker)
- [Metodología de trabajo](#-metodología-de-trabajo)
- [Pruebas](#-pruebas)
- [Roadmap](#-roadmap)
- [Autor](#-autor)
- [Licencia](#-licencia)

---

## 📖 Descripción

El sistema centraliza tres procesos que hoy viven dispersos en correos, documentos impresos y carpetas locales:

- **Gestión de planeación didáctica**: registro por parte del docente, revisión y aprobación por coordinación, historial por ciclo.
- **Control de avances por parcial**: estado de cumplimiento (cumplido / parcial / no cumplido) con reportes gráficos.
- **Evidencias de capacitación docente**: carga de constancias, validación por coordinación académica.
- **Reportes institucionales**: vistas consolidadas para coordinación y dirección, exportables a PDF y Excel.

## 🛠 Stack tecnológico

| Capa | Tecnología |
|---|---|
| FrontEnd | React + React Router |
| BackEnd | Java 17 + Spring Boot (Web, Security, Data JPA) |
| Base de datos | PostgreSQL, migraciones con Flyway |
| Autenticación | JWT + OAuth2 |
| Documentación de API | springdoc-openapi (Swagger UI) |
| Reportes | Apache PDFBox · Apache POI |
| Contenedores | Docker + docker-compose |
| CI/CD | GitHub Actions |
| Calidad de código | ESLint · Prettier · Husky · Commitlint |

## 🏗 Arquitectura

Arquitectura multicapa: presentación (React) → lógica de negocio (Spring Boot) → datos (PostgreSQL vía JPA) / integración (APIs externas y Web Services propios), con una capa transversal de pruebas (JUnit + Postman/Newman).

```
┌─────────────────────────────┐
│     Capa de presentación    │  React (SPA)
├─────────────────────────────┤
│  Capa de lógica de negocio  │  Spring Boot (controllers/services)
├───────────────┬─────────────┤
│  Capa de datos │ Integración │  PostgreSQL/JPA │ APIs + Web Services
└───────────────┴─────────────┘
        entorno: Docker + nube
```

El diagrama completo está en `docs/diagrama_arquitectura.png`.

## 📂 Estructura del repositorio

```
proyecto-web-integral/
├── client/              # Frontend React
│   ├── src/
│   ├── .eslintrc.json
│   └── package.json
├── server/               # Backend Spring Boot
│   ├── src/main/java/...
│   ├── src/main/resources/
│   └── pom.xml
├── docs/                 # Diagramas y documentación
├── .husky/               # Git hooks (lint-staged, commitlint)
├── docker-compose.yml
├── commitlint.config.js
└── README.md
```

## ✅ Requisitos previos

- Node.js 18+ y npm
- Java 17+ y Maven
- PostgreSQL 16 (o Docker, ver abajo)
- Git + llave SSH configurada con GitHub

## ⚙️ Instalación y configuración

### Backend

```bash
cd server
cp src/main/resources/application-example.properties src/main/resources/application.properties
# Edita application.properties con tus credenciales de PostgreSQL
mvn clean install
mvn spring-boot:run
```

El backend queda disponible en `http://localhost:8080`. Documentación interactiva en `http://localhost:8080/swagger-ui.html`.

### Frontend

```bash
cd client
npm install
npm run dev
```

El frontend queda disponible en `http://localhost:5173`.

### Git hooks (una sola vez, en la raíz)

```bash
npm install
npx husky init
```

## 🔑 Variables de entorno

Crea `server/src/main/resources/application.properties` a partir del ejemplo, con al menos:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/seguimiento_docente
spring.datasource.username=postgres
spring.datasource.password=********
jwt.secret=********
jwt.expiration-ms=3600000
spring.security.oauth2.client.registration.google.client-id=********
spring.security.oauth2.client.registration.google.client-secret=********
```

## 🐳 Ejecución con Docker

```bash
docker-compose up --build
```

Levanta el frontend, el backend y la base de datos PostgreSQL en contenedores separados.

## 🔄 Metodología de trabajo

- **Kanban** con cortes de entrega por parcial (ver tablero en GitHub Projects).
- Ramas: `main` (producción) ← `develop` (integración) ← `feature/*`, `fix/*`, `docs/*`.
- Todo cambio entra por Pull Request; `main` y `develop` están protegidas con Rulesets.
- Commits siguiendo [Conventional Commits](https://www.conventionalcommits.org/), validados con Commitlint.
- CI en GitHub Actions: pruebas, lint y fusión automática de PRs aprobados.

## 🧪 Pruebas

```bash
# Backend
cd server && mvn test

# Colección funcional (requiere Newman instalado)
newman run docs/postman_collection.json
```

## 🗺 Roadmap

- [x] Sprint 1 — MVP: autenticación y base de datos
- [ ] Sprint 2 — Core: panel de usuario y lógica de negocio
- [ ] Sprint 3 — Release: notificaciones y despliegue

## 👤 Autor

**Luis Manuel López Cano**
Ingeniería en Desarrollo y Gestión de Software · UTNG
[GitHub](https://github.com/manuellopez-dev)

## 📄 Licencia

Proyecto académico — Universidad Tecnológica del Norte de Guanajuato. Uso educativo.
