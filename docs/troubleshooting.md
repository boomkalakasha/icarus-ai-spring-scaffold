# Troubleshooting

## Maven or Java is missing

Use JDK 17 and the checked-in wrapper. POSIX shells use `./mvnw`; Windows
PowerShell uses `./mvnw.cmd`. If the wrapper distribution cannot be downloaded,
install Maven 3.9+ and retry with `mvn`.

```powershell
.\mvnw.cmd -B -ntp clean verify
python -m unittest discover -s scripts -p "test_*.py"
```

## The CLI output file is rejected

Pass a single new filename such as `demo-service.zip` from the directory in
which the CLI is running. Do not pass a directory, absolute path, parent
traversal, non-ZIP suffix or an existing file. Use stdout redirection when a
different destination policy is required:

```bash
java -jar icarus-scaffold-cli/target/icarus-scaffold-cli-1.1.0-all.jar > demo-service.zip
```

## Runtime smoke cannot connect

The sample verifier selects a bounded free local port for its default run,
checks the packaged `boot` jar and terminates it in a `finally` block. Choose a
different validated port through `ICARUS_CLI_ARGS_JSON` if another local
service owns the port, then inspect the temporary `runtime.log` while
reproducing the failure.

## Docker is not available

Docker checks are optional. When the `docker` executable, daemon or Compose
plugin is unavailable, the sample report records `NOT_RUN` and the script does
not claim container coverage. When Docker is available, the verifier parses
Compose, builds the image, waits for a healthy container and runs Compose
cleanup even after a failure.

## REST smoke fails

Confirm that the server is listening on `127.0.0.1:8080`, send
`Content-Type: application/json`, and use only the documented request fields.
The REST adapter returns ZIP bytes and does not create a server-side output
file. See [REST API](rest-api.md) and [SUPPORT.md](../SUPPORT.md).
