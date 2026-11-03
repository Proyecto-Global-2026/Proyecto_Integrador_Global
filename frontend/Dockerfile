# ===== ETAPA 1: BUILD =====
FROM node:22-alpine AS build
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

# ===== ETAPA 2: SERVE CON NGINX =====
FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html

# Limpiar html por defecto
RUN rm -rf ./*

# Copiar build
COPY --from=build /app/dist .

# Copiar configuración nginx
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
