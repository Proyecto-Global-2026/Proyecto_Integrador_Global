# ===== ETAPA 1: BUILD =====
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app

# Descargar dependencias primero (mejor cache)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copiar código fuente y compilar
COPY src ./src
RUN mvn clean package -DskipTests

# ===== ETAPA 2: RUNTIME =====
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copiar jar generado
COPY --from=build /app/target/*.jar app.jar

# Exponer puerto del backend
EXPOSE 8080

# Variables por defecto (se pueden sobreescribir con docker-compose/.env)
ENV SPRING_PROFILES_ACTIVE=default
ENV JAVA_OPTS="-Xms256m -Xmx512m"

# Arrancar app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
