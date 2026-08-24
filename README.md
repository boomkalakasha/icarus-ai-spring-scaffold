# Icarus AI Spring Scaffold

`icarus-ai-spring-scaffold` is a security-focused, AI-friendly generator for
small Spring Boot projects. It validates the project coordinates and writes a
ZIP archive in memory before returning it. The generated project is intended as
a reviewable starting point for a Java 17 / Spring Boot 3 application; it is
not a hosted service or a deployment guarantee.

The repository is released under the [Apache License 2.0](LICENSE). The
initial stable release line is `v1.0.0` and follows SemVer.

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
- Maven 3.6.3 or newer, or the checked-in Maven wrapper when available.
- Python 3.10+ for the repository verification scripts.
- Docker and Compose are optional and are only needed when validating a
  generated project's container files.

All build dependencies are expected to resolve from Maven Central. No private
repository, private binary or organization-specific service is required by
the public build.

## Build and test

```bash
mvn -B -ntp clean verify
python scripts/test_generate_sample.py
```

On Windows PowerShell, the equivalent is:

```powershell
mvn -B -ntp clean verify
```

The default GitHub workflow builds the full Maven reactor and runs tests. It
then generates a sample project, builds and tests that sample, and runs the
public-content scanner over the source and generated material.

## Generate a ZIP locally

The CLI writes ZIP bytes to standard output. When using an executable CLI
artifact that includes its runtime dependencies, redirect that output to a
file:

```bash
java -jar icarus-scaffold-cli/target/icarus-scaffold-cli-1.0.0-all.jar \
  --artifact demo-service \
  --group com.example.demo \
  --package com.example.demo \
  --port 8080 \
  --description "Generated sample" > demo-service.zip
```

The exact jar filename includes the project version. Run the CLI's help
command if an option has changed in a later compatible release. The command
creates one ZIP archive on stdout; it does not receive an output path, a
template directory or a server filesystem path.

For a platform-neutral end-to-end check, run:

```bash
python scripts/generate-sample.py --root .
```

The script locates the CLI jar, validates ZIP entry paths, extracts the sample
into a temporary/generated verification directory and runs Maven tests in the
generated project. Set `ICARUS_CLI_ARGS_JSON` to a JSON array of CLI options if
a downstream-compatible CLI needs a different argument layout; the ZIP still
must be written to stdout.

## Run the optional REST adapter

```bash
java -jar icarus-scaffold-server/target/icarus-scaffold-server-1.0.0.jar
curl --fail-with-body -H "Content-Type: application/json" \
  --data '{"artifact":"demo-service","group":"com.example.demo","package":"com.example.demo","port":8080,"description":"Demo service"}' \
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
