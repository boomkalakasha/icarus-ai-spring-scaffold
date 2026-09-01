# Generated project

The generator creates a Java 17 / Spring Boot 3 project in one of two
deterministic profiles:

- `simple` is one Maven module whose packages keep the domain, application,
  infrastructure, API and boot boundaries together.
- `modular` keeps those same boundaries as five Maven modules and remains the
  default when `profile` is omitted, preserving the existing output contract.

The modular profile's project tree is:

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

## AI guidance coverage

Each generated archive has one root `AGENTS.md` that maps the stock layers and
their inward dependency direction. It intentionally does not create a separate
guide for every generated module or package: the default modules share the
same build, test, delivery and safety boundaries. Add a nearest module-level
guide later only when that module obtains a distinct command, external
contract, data/security boundary, release or ownership lifecycle, or a
dependency direction that the root guide cannot describe safely. Link any such
guide back to the root instead of copying project rules.

## Build and run

```bash
mvn -B -ntp package
java -jar boot/target/boot-0.1.0-SNAPSHOT.jar
```

For a simple-profile archive, the equivalent commands are `mvn -B -ntp
package` and `java -jar target/<artifact>-0.1.0-SNAPSHOT.jar`.

The Scaffold does not include or host an AI model. A CLI or REST request is a
deterministic, reproducible entry point; a separate host tool may help turn a
conversation into that confirmed request.

The bounded local smoke path is:

- `GET /actuator/health` → `{"status":"UP"}`;
- `GET /api/greetings?subject=sample` → `{"subject":"sample","message":"Hello, sample!"}`.

Compose uses the generated JRE-only `docker/HealthCheck.java` probe, so the
runtime image does not depend on `curl` or `wget`. Review dependencies,
configuration, support ownership and deployment controls before using the
generated project outside a local development environment.
