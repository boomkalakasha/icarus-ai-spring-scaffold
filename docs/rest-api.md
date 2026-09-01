# REST API

The optional `icarus-scaffold-server` binds to `127.0.0.1` by default. Start
the 1.2.0 candidate jar after building the reactor:

```text
java -jar icarus-scaffold-server/target/icarus-scaffold-server-1.2.0.jar
```

## Generate a ZIP

`POST /api/scaffolds` (the compatibility alias `/api/scaffold` is also
available) accepts JSON and returns `application/zip`:

```json
{
  "artifact": "demo-service",
  "group": "com.example.demo",
  "package": "com.example.demo",
  "port": 18080,
  "description": "Generated sample",
  "templatePack": "default",
  "profile": "simple"
}
```

The default request emits no `LICENSE`. To make an explicit downstream
license declaration, include the complete atomic set:

```json
{
  "artifact": "demo-service",
  "group": "com.example.demo",
  "package": "com.example.demo",
  "port": 18080,
  "description": "Generated sample",
  "license": "Apache-2.0",
  "copyrightHolder": "Example Labs",
  "copyrightYear": 2026
}
```

Supported license identifiers are `Apache-2.0` and `MIT`. Supplying only
part of the declaration returns a validation error rather than guessing
ownership or license terms.

`templatePack` is optional and defaults to `default`. `profile` is optional and
defaults to `modular` for backwards-compatible output. Set it to `simple` for
one Maven module, or `modular` for the existing five-module layout; both
profiles keep the same greeting and health slice. The stock server property
`icarus.scaffold.allowed-template-packs` defaults to `default`; configure a
comma-separated allow-list such as `default,company-java` only for trusted
classpath packs supplied by the hosting application. The environment-variable
form is `ICARUS_SCAFFOLD_ALLOWED_TEMPLATE_PACKS`. A pack that is not on the
allow-list, or an unknown pack ID, is rejected before generation.

The response has `Content-Disposition: attachment; filename=demo-service.zip`
and `Cache-Control: no-store`. Unknown fields, invalid coordinates, output
paths, overwrite instructions and shell/template options are rejected. Request
size limits and security response headers are applied before generation.

The server never accepts a template directory, arbitrary filesystem path, URL,
shell command or runtime external JAR path. Pack templates are loaded through
the trusted process classpath only.

The REST adapter does not include or host an AI model. Callers must send the
validated request fields explicitly; a separate host tool can translate a
conversation into that request before calling this endpoint.

This adapter returns bytes; it does not write a server-side file. If exposed
beyond localhost, add authentication, TLS, rate limiting and operational
monitoring at a trusted edge.
