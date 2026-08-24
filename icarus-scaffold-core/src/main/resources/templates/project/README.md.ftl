# ${artifact}

${description}

This Java 17 Spring Boot project is split into five small modules:

- `domain` — domain values and invariants
- `application` — use cases
- `infrastructure` — adapters
- `api` — HTTP transport
- `boot` — executable Spring Boot application

## Run

```text
mvn test
mvn spring-boot:run -pl boot
```

The sample endpoint is `GET /api/greetings?subject=team`. Health is exposed
at `GET /actuator/health`. The default HTTP port is `${port}`.

## Container

```text
docker build -t ${artifact}:local .
docker compose up --build
```

The compose file has no stateful service and does not require credentials.
Review the generated source and dependency list before publishing or deploying
it.
