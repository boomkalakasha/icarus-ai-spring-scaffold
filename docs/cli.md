# CLI

The executable artifact is `icarus-scaffold-cli/target/icarus-scaffold-cli-1.1.0-all.jar`.
Run `--help` for the complete option list.

## Quick start

POSIX shell:

```bash
java -jar icarus-scaffold-cli/target/icarus-scaffold-cli-1.1.0-all.jar \
  --artifact demo-service --group com.example.demo \
  --package com.example.demo --port 18080 \
  --description "Generated sample" --output demo-service.zip
```

PowerShell:

```powershell
java -jar .\icarus-scaffold-cli\target\icarus-scaffold-cli-1.1.0-all.jar `
  --artifact demo-service --group com.example.demo `
  --package com.example.demo --port 18080 `
  --description "Generated sample" --output demo-service.zip
```

`--output` is optional. Without it, the ZIP bytes are written to stdout and no
file is opened. With it, the value must be exactly one relative filename whose
case-insensitive suffix is `.zip`, directly under the current working
directory. The CLI rejects absolute paths, `/` or `\` separators, `.`/`..`
segments, non-ZIP names and existing targets. The file is opened with
`CREATE_NEW`, so a concurrent creator cannot turn validation into overwrite.

The CLI does not accept arbitrary output directories, overwrite flags, shell
commands, template-directory overrides or server filesystem paths.
