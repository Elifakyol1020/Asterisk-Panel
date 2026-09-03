# Expects an npm frontend with a build script that produces dist/ (for example Vite).
FROM node:22-alpine AS build
WORKDIR /app

COPY asterisk-frontend/package.json asterisk-frontend/package-lock.json ./
RUN npm ci

COPY asterisk-frontend/ ./
ENV VITE_API_BASE_URL=/api
RUN npm run build

FROM nginx:stable-alpine AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
