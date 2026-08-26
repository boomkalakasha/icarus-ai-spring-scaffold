# Architecture

Icarus AI Spring Scaffold is a small Maven reactor with three deliberately
separate modules:

```text
ScaffoldRequest -> core validator -> fixed FreeMarker manifest -> ZIP bytes
       |                                  ^
       +-> CLI (stdout or safe cwd file)  |
       +-> REST server (application/zip)  |
                                          +-- generated five-module project
```

## Modules

- `icarus-scaffold-core` owns request validation, the bundled template
  manifest, path confinement and deterministic in-memory ZIP generation. It
  has no HTTP, database or process-execution dependency.
- `icarus-scaffold-cli` is a thin Picocli adapter. Stdout remains the default
  byte stream. Its optional `--output` target is one new ZIP filename directly
  under the process working directory.
- `icarus-scaffold-server` is an optional Spring Boot adapter. It returns ZIP
  bytes and never accepts a server path, overwrite switch, command or template
  directory.

## Generated project

The fixed manifest renders `domain`, `application`, `infrastructure`, `api`
and `boot` modules plus tests, public governance files, Docker/Compose and
agent guidance. User values are bounded before FreeMarker sees them. Template
paths are normalized below a temporary root and ZIP entries are validated as
relative paths.

## Evidence boundary

The repository build and generated sample checks prove source/build behavior in
the current environment. Runtime smoke is bounded to health and greeting
requests. Docker checks run only when both Docker and Docker Compose are
available; otherwise the sample report records `NOT_RUN`. None of these local
checks is a production deployment or support guarantee.
