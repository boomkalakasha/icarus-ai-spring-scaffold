# Support

This project was generated from Icarus AI Spring Scaffold. Please keep
downstream support information specific to the team that owns this generated
service; the scaffold maintainers do not operate it for you.

## Before asking for help

- Record the generator release or commit and the generated inputs, with
  credentials and private values removed.
- Include the operating system, JDK, Maven and (when relevant) Docker versions.
- Describe whether the issue is in generated source, a local build, HTTP
  runtime, or deployment.
- Attach the smallest safe reproduction, relevant command output and a ZIP
  listing rather than credentials or private configuration.

Useful local checks are:

```text
mvn -B -ntp verify
mvn -B -ntp package
```

The generated service exposes `GET /actuator/health` and
`GET /api/greetings?subject=team` for local smoke checks. A successful local
check is evidence about this checkout only; it is not a production-support or
deployment guarantee.

## Security reports

Do not publish exploit details, credentials, personal data or private URLs in
an issue. Follow `SECURITY.md` and use the private reporting path configured by
the owner of this generated service.
