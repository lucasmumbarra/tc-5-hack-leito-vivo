# LeitoVivo

Gestão de leitos hospitalares em tempo real para o SUS.

## Stack

- Java 21 · Spring Boot 4.1.0 · PostgreSQL 16 · Flyway · Docker Compose

## Subir o ambiente

```bash
cp .env.example .env
docker compose up --build
```

A aplicação só inicia depois do healthcheck do Postgres. O Flyway aplica `V1__schema.sql` e `V2__seed_sla_default.sql` na subida.

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI: http://localhost:8080/v3/api-docs

## Build local (sem Docker)

```bash
mvn -q package
```

Requer PostgreSQL acessível conforme `src/main/resources/application.yml` (ou variáveis `SPRING_DATASOURCE_*`).
