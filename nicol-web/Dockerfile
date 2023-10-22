FROM oven/bun:latest AS builder-stage
ENV BACKEND_URL=http://api:9005
WORKDIR /app
COPY . .
RUN bun install
RUN bun run build:poc

FROM nginx:1.20-alpine AS production-stage
COPY --from=builder-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf