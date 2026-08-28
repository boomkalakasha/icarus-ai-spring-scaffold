# Generated project

The generator creates a Java 17 / Spring Boot 3 multi-module project:

```text
domain/          domain values and invariants
application/     use cases
infrastructure/  adapters
api/             HTTP transport
boot/            executable application
docker/          JRE-only container health probe
```

The generated archive also contains `SUPPORT.md`, `SECURITY.md`, bilingual
READMEs, tests, Dockerfile and Compose configuration. It contains no
`LICENSE` by default. When the caller explicitly supplies a supported
license, rights holder and year together, the archive contains the selected
`Apache-2.0` or `MIT` license and the generated README records that choice.

## Build and run

```bash
mvn -B -ntp package
java -jar boot/target/boot-0.1.0-SNAPSHOT.jar
```

The bounded local smoke path is:

- `GET /actuator/health` → `{"status":"UP"}`;
- `GET /api/greetings?subject=sample` → `{"subject":"sample","message":"Hello, sample!"}`.

Compose uses the generated JRE-only `docker/HealthCheck.java` probe, so the
runtime image does not depend on `curl` or `wget`. Review dependencies,
configuration, support ownership and deployment controls before using the
generated project outside a local development environment.
