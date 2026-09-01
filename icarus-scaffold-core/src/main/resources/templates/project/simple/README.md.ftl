# ${artifact}

${description}

[中文说明](README.zh-CN.md) · [Support](SUPPORT.md) · [Security](SECURITY.md)

## Quick start

This ZIP uses the simple profile: one Maven module with domain, application,
infrastructure, API and boot packages. It keeps the first useful service small;
choose the `modular` profile when those layers need independent module
boundaries.

```bash
mvn -B -ntp package
java -jar target/${artifact}-0.1.0-SNAPSHOT.jar
```

Then check `http://127.0.0.1:${port}/actuator/health` and
`http://127.0.0.1:${port}/api/greetings?subject=team`.

The Scaffold does not interpret natural-language requirements or call an AI
service. A Vibe Coding tool may help turn a conversation into confirmed CLI
arguments, but the command that selected this profile is reproducible without
an AI service:

```text
icarus-scaffold --profile simple --artifact ${artifact} --group ${group} --package ${packageName} --port ${port}
```

## Run and container

```text
mvn test
mvn spring-boot:run
docker build -t ${artifact}:local .
docker compose up --build
```

The sample endpoint is `GET /api/greetings?subject=team`. Health is exposed
at `GET /actuator/health`. The default HTTP port is `${port}`.

The image contains a small JRE-only health probe, so Compose does not depend
on `curl`, `wget` or an extra package. The compose file has no stateful service
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
