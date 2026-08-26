# Icarus AI Spring Scaffold

[中文说明](README.zh-CN.md) · [Architecture](docs/architecture.md) · [Support](SUPPORT.md)

`icarus-ai-spring-scaffold` is a security-focused, AI-friendly generator for
small Spring Boot projects. It validates the project coordinates and writes a
ZIP archive in memory before returning it. The generated project is intended as
a reviewable starting point for a Java 17 / Spring Boot 3 application; it is
not a hosted service or a deployment guarantee.

The repository is released under the [Apache License 2.0](LICENSE). The latest
public stable release is [`v1.1.0`](https://github.com/boomkalakasha/icarus-ai-spring-scaffold/releases/tag/v1.1.0),
built from its reviewed tag with checksums, SBOM, and build provenance. Versions follow SemVer.

## Quick start (60 seconds)

Build the public reactor, then generate one sample ZIP. The default CLI mode
still writes byte-compatible ZIP output to stdout.

POSIX shell:

```bash
./mvnw -B -ntp clean verify
java -jar icarus-scaffold-cli/target/icarus-scaffold-cli-1.1.0-all.jar \
  --artifact demo-service --group com.example.demo \
  --package com.example.demo --port 18080 \
  --description "Generated sample" --output demo-service.zip
```

PowerShell:

```powershell
.\mvnw.cmd -B -ntp clean verify
java -jar .\icarus-scaffold-cli\target\icarus-scaffold-cli-1.1.0-all.jar `
  --artifact demo-service --group com.example.demo `
  --package com.example.demo --port 18080 `
  --description "Generated sample" --output demo-service.zip
```

`--output` accepts only one new `*.zip` filename directly below the current
working directory. Absolute, nested, `..`, non-ZIP and existing targets are
rejected; `CREATE_NEW` protects against a validation/write race. Omit it to
preserve the original stdout ZIP contract.

## What is here

| Module | Responsibility |
| --- | --- |
| `icarus-scaffold-core` | Input validation, FreeMarker rendering, safe paths and ZIP generation. It has no web or database requirement. |
| `icarus-scaffold-cli` | Local command-line entry point that writes a generated ZIP; it does not overwrite an arbitrary directory. |
| `icarus-scaffold-server` | Optional REST adapter. It returns a ZIP, applies request limits and security headers, and does not accept a server filesystem path. |
| `icarus-scaffold-core/.../templates/` | The generated multi-module project and its public development documentation. |

The generator deliberately keeps the core independent from HTTP and database
concerns. Generated projects contain the application layers and tests needed
for a useful starting point, but each generated project still needs normal
dependency, security, operational and license review before production use.

## Requirements

- JDK 17 (a Temurin or other compatible OpenJDK distribution is suitable).
- Maven 3.9+ or the checked-in Maven wrapper when available.
- Python 3.10+ for the repository verification scripts.
- Docker and Compose are optional and are only needed when validating a
  generated project's container files.

All build dependencies are expected to resolve from Maven Central. No private
repository, private binary or organization-specific service is required by
the public build.

## Build and test

```bash
./mvnw -B -ntp clean verify
python -m unittest discover -s scripts -p "test_*.py"
```

On Windows PowerShell, the equivalent is:

```powershell
.\mvnw.cmd -B -ntp clean verify
python -m unittest discover -s scripts -p "test_*.py"
```

The default GitHub workflow builds the full Maven reactor and runs tests. It
then generates a sample project, builds and tests that sample, and runs the
public-content scanner over the source and generated material.

## Generate a ZIP locally

The CLI writes ZIP bytes to standard output by default. When using an
executable CLI artifact that includes its runtime dependencies, redirect that
output to a file when you need a destination outside the cwd filename policy:

```bash
java -jar icarus-scaffold-cli/target/icarus-scaffold-cli-1.1.0-all.jar \
  --artifact demo-service \
  --group com.example.demo \
  --package com.example.demo \
  --port 18080 \
  --description "Generated sample" > demo-service.zip
```

The exact jar filename includes the project version. Run the CLI's help
command if an option has changed in a later compatible release. The safer
in-process path is `--output demo-service.zip` shown above. The command does
not accept an arbitrary output directory, overwrite switch, template
directory, shell command or server filesystem path.

For a platform-neutral end-to-end check, run:

```bash
python scripts/generate-sample.py --root .
```

The script locates the CLI jar, validates ZIP entry paths, extracts the sample
into a temporary/generated verification directory and runs Maven `package` in
the generated project. It then starts the packaged app for bounded health and
greeting checks. Docker Compose parsing, image build and container-health
checks run only when Docker and Compose are available; otherwise the report
records `NOT_RUN`. Temporary apps, containers and verification directories are
cleaned up. Set `ICARUS_CLI_ARGS_JSON` to a JSON array of CLI options if a
downstream-compatible CLI needs a different argument layout; the ZIP still
must be written to stdout.

## Run the optional REST adapter

```bash
java -jar icarus-scaffold-server/target/icarus-scaffold-server-1.1.0.jar
curl --fail-with-body -H "Content-Type: application/json" \
  --data '{"artifact":"demo-service","group":"com.example.demo","package":"com.example.demo","port":18080,"description":"Demo service"}' \
  http://localhost:8080/api/scaffolds --output demo-service.zip
```

The server accepts JSON only, rejects unknown fields, returns `application/zip`,
and never accepts an output directory or overwrite instruction. It binds to
`127.0.0.1` by default. If you deliberately expose it on a network, set
`SERVER_ADDRESS` and add authentication, rate limiting, TLS and operator-level
monitoring at your trusted edge; this repository is not a turnkey public
multi-tenant service.

## Security boundaries

Inputs such as artifact, group, package, port and description are bounded and
validated. Template paths are normalized and must remain under a temporary
root; ZIP entries cannot be absolute or contain parent traversal. The public
generator does not accept shell commands, arbitrary template directories,
overwrite flags or generated default credentials. Requests are not logged in
full.

These are source-level and testable boundaries. Review the generated project,
its dependencies and its runtime configuration before using it for a real
service.

## Contributing and support

Please read [CONTRIBUTING.md](CONTRIBUTING.md) before opening a pull request.
Security issues belong in [SECURITY.md](SECURITY.md), not in a public issue.
For usage questions and reproducible build problems, see [SUPPORT.md](SUPPORT.md).

## Releases

Release tags use `vMAJOR.MINOR.PATCH`. Release automation builds from the
exact tag, emits SHA-256 checksums, produces a CycloneDX SBOM and publishes a
GitHub build-provenance attestation where the repository settings support it.
Those assets prove how the tagged source was built; a GitHub Release does not
prove production deployment, customer delivery or traffic cutover.

See [CHANGELOG.md](CHANGELOG.md) for offline release notes.

Detailed contracts are in [CLI](docs/cli.md), [REST API](docs/rest-api.md),
[generated project](docs/generated-project.md) and
[troubleshooting](docs/troubleshooting.md). Stable local brand copies are
kept under [docs/assets/brand](docs/assets/brand/).
