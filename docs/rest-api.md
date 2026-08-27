# REST API

The optional `icarus-scaffold-server` binds to `127.0.0.1` by default. Start
the 1.1.3 jar after building the reactor:

```text
java -jar icarus-scaffold-server/target/icarus-scaffold-server-1.1.3.jar
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
  "description": "Generated sample"
}
```

The response has `Content-Disposition: attachment; filename=demo-service.zip`
and `Cache-Control: no-store`. Unknown fields, invalid coordinates, output
paths, overwrite instructions and shell/template options are rejected. Request
size limits and security response headers are applied before generation.

This adapter returns bytes; it does not write a server-side file. If exposed
beyond localhost, add authentication, TLS, rate limiting and operational
monitoring at a trusted edge.
