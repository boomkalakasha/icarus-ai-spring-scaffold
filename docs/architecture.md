# Architecture

Icarus AI Spring Scaffold is a small Maven reactor with three deliberately
separate modules:

```text
ScaffoldRequest -> core validator -> allow-listed TemplatePack/profile -> ZIP bytes
       |                                      ^
       +-> CLI (stdout or safe cwd file)      |
       +-> REST server (application/zip)      |
       TemplatePackRegistry (trusted classpath only)
```

## Modules

- `icarus-scaffold-core` owns request validation, the trusted-classpath
  `TemplatePack` SPI and registry, path confinement and deterministic
  in-memory ZIP generation. It has no HTTP, database or process-execution
  dependency.
- `icarus-scaffold-cli` is a thin Picocli adapter. Stdout remains the default
  byte stream. Its optional `--output` target is one new ZIP filename directly
  under the process working directory.
- `icarus-scaffold-server` is an optional Spring Boot adapter. It returns ZIP
  bytes and never accepts a server path, overwrite switch, command or template
  directory.

## Template packs

`TemplatePack` implementations expose an identifier and a list of
`TemplateDefinition(templatePath, outputPath)` values. Both paths are safe
relative logical names: the renderer resolves templates only through its
bundled classpath loader and writes only below its temporary root. A pack may
provide a profile-specific manifest through `templatesFor`; the bundled
`default` pack provides `simple` and `modular` profiles. The stock
`TemplatePackRegistry.defaults()` contains only `default`; a trusted
downstream can add classpath providers with `TemplatePackRegistry.fromClasspath()`
and then allow selected identifiers at its own server edge. Duplicate pack IDs,
duplicate output paths, unsafe paths and unknown request IDs fail closed.

The request field `templatePack` and the CLI `--template-pack` option default to
`default`; the request field `profile` and CLI `--profile` option default to
`modular` for backwards-compatible output. `simple` is one Maven module and
`modular` is five modules. No template directory, arbitrary filesystem path,
URL, shell command or runtime external JAR path is accepted. The CLI remains a
deterministic no-AI entry point; natural-language interpretation belongs to a
separate host tool.

## Generated project

The `modular` manifest renders `domain`, `application`, `infrastructure`, `api`
and `boot` modules plus tests. The `simple` manifest places the same packages
inside one Maven module. Both include public governance files, Docker/Compose
and agent guidance. User values are bounded before FreeMarker sees them.
Template paths are normalized below a temporary root and ZIP entries are
validated as relative paths.

## Evidence boundary

The repository build and generated sample checks prove source/build behavior in
the current environment. Runtime smoke is bounded to health and greeting
requests. Docker checks run only when both Docker and Docker Compose are
available; otherwise the sample report records `NOT_RUN`. None of these local
checks is a production deployment or support guarantee.
