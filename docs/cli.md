# CLI

The executable artifact is `icarus-scaffold-cli/target/icarus-scaffold-cli-1.1.4-all.jar`.
Run `--help` for the complete option list.

## Quick start

POSIX shell:

```bash
java -jar icarus-scaffold-cli/target/icarus-scaffold-cli-1.1.4-all.jar \
  --artifact demo-service --group com.example.demo \
  --package com.example.demo --port 18080 \
  --description "Generated sample" --output demo-service.zip
```

PowerShell:

```powershell
java -jar .\icarus-scaffold-cli\target\icarus-scaffold-cli-1.1.4-all.jar `
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

Generated-project licensing is opt-in. The default ZIP contains no `LICENSE`
and makes no Icarus copyright claim. Provide all three options together only
after the actual rights holder has made the decision:

```text
--license MIT --copyright-holder "Example Authors" --copyright-year 2026
```

`--license` accepts `Apache-2.0` or `MIT`. A partial declaration,
unsupported identifier, blank holder, control character, or year outside
1900–9999 is rejected.

The CLI does not accept arbitrary output directories, overwrite flags, shell
commands, template-directory overrides or server filesystem paths.
