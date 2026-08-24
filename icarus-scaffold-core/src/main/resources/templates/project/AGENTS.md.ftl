# Generated service agent guide

This project is a small Spring Boot service generated from a public template.

## Boundaries

- `domain` owns business types and invariants.
- `application` owns use cases and orchestration.
- `infrastructure` owns adapters and persistence implementations.
- `api` owns HTTP transport and request mapping.
- `boot` owns application wiring and the process entry point.

Keep dependencies pointing inward. Add tests beside the module that owns the
behavior. Do not add credentials, private repositories, shell execution, or
machine-specific paths to this project.

## Local verification

```text
mvn test
mvn package
```

Run the application only when you need to inspect the HTTP contract. The
health endpoint is `/actuator/health` and the sample API is `/api/greetings`.

## Optional GitHub open-source delivery

When this generated project is intentionally maintained as a public GitHub
repository, protect `main`, merge through reviewed pull requests, and use
truthful Conventional Commits. Release immutable `vMAJOR.MINOR.PATCH` tags only
after required checks pass; build artifacts from the exact tag and attach
checksums. Keep customer configuration and private deployment automation in a
private downstream repository. A GitHub Release proves publication of source
or artifacts, not production deployment or traffic cutover.

If the project remains internal, follow its closest repository Git/CI rules
instead. Do not silently replace an internal delivery policy with GitHub Flow.
