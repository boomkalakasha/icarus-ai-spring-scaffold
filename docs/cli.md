# CLI

The executable artifact is `icarus-scaffold-cli/target/icarus-scaffold-cli-1.2.0-all.jar`.
Run `--help` for the complete option list.

## Quick start

POSIX shell:

```bash
java -jar icarus-scaffold-cli/target/icarus-scaffold-cli-1.2.0-all.jar \
  --artifact demo-service --group com.example.demo \
  --package com.example.demo --port 18080 \
  --description "Generated sample" --profile simple --output demo-service.zip
```

PowerShell:

```powershell
java -jar .\icarus-scaffold-cli\target\icarus-scaffold-cli-1.2.0-all.jar `
  --artifact demo-service --group com.example.demo `
  --package com.example.demo --port 18080 `
  --description "Generated sample" --profile simple --output demo-service.zip
```

`--output` is optional. Without it, the ZIP bytes are written to stdout and no
file is opened. With it, the value must be exactly one relative filename whose
case-insensitive suffix is `.zip`, directly under the current working
directory. The CLI rejects absolute paths, `/` or `\` separators, `.`/`..`
segments, non-ZIP names and existing targets. The file is opened with
`CREATE_NEW`, so a concurrent creator cannot turn validation into overwrite.

Generated-project licensing is opt-in. The default ZIP contains no `LICENSE`
and makes no Icarus copyright claim. Provide all three options together only
after the actual rights holder has made the decision:

```text
--license MIT --copyright-holder "Example Authors" --copyright-year 2026
```

`--license` accepts `Apache-2.0` or `MIT`. A partial declaration,
unsupported identifier, blank holder, control character, or year outside
1900–9999 is rejected.

## Architecture profile

`--profile` accepts `simple` or `modular` and defaults to `modular` so existing
requests keep their five-module output. `simple` produces one Maven module with
domain, application, infrastructure, API and boot packages; `modular` keeps
those layers as five Maven modules. Both profiles include the same greeting and
health slice and are deterministic for the same request.

The CLI is the no-AI reproducible entry point. Scaffold does not include or host
an AI model and does not interpret natural-language requirements. A Vibe Coding
tool or IDEA plugin may prepare the confirmed arguments, but it is not required
to run the command.

## Template pack selection

`--template-pack` selects a trusted classpath pack identifier and defaults to
`default`:

```text
icarus-scaffold --template-pack default --artifact demo-service
```

The stock CLI registry contains only `default`. An unknown identifier is
rejected before any ZIP bytes are written. Packs are application/classpath
extensions represented by `TemplatePack`; the CLI does not accept a template
directory, arbitrary filesystem path, URL, shell command or external runtime
JAR path.

The CLI does not accept arbitrary output directories, overwrite flags, shell
commands, template-directory overrides or server filesystem paths.
