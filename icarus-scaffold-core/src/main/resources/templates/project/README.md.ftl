# ${artifact}

${description}

[中文说明](README.zh-CN.md) · [Support](SUPPORT.md) · [Security](SECURITY.md)

## Quick start (60 seconds)

```bash
mvn -B -ntp package
java -jar boot/target/boot-0.1.0-SNAPSHOT.jar
```

Then check `http://127.0.0.1:${port}/actuator/health` and
`http://127.0.0.1:${port}/api/greetings?subject=team`.

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

The image contains a small JRE-only health probe, so Compose does not depend on
`curl`, `wget` or an extra package. The compose file has no stateful service
and does not require credentials. Review the generated source and dependency
list before publishing or deploying it.

## Support and license

See [SUPPORT.md](SUPPORT.md) and [SECURITY.md](SECURITY.md) for the downstream
service's local support and security boundaries.
<#if licenseDeclared>
This generated project declares the `${license}` license in [LICENSE](LICENSE),
with copyright attributed to `${copyrightHolder}` for ${copyrightYear}.
<#else>
No project license was declared during generation, so this ZIP intentionally
contains no `LICENSE`. The downstream rights holder must make and document the
licensing decision before public distribution.
</#if>
