# Contributing

Thanks for helping improve Icarus AI Spring Scaffold. Contributions should
make the public generator safer, easier to review or more useful without
introducing private infrastructure or undocumented behavior.

## Before you start

1. Search existing issues and pull requests for related work.
2. For a behavior change, describe the input contract, generated output and
   security impact before implementing it.
3. Keep the change focused. Do not copy private downstream configuration,
   customer data, credentials, proprietary binaries or private build URLs.

## Development environment

- JDK 17 and Maven 3.9+.
- Python 3.10+ for repository scripts.
- Docker is optional and only needed for generated-project container checks.

Build and test the full reactor:

```bash
./mvnw -B -ntp clean verify
python scripts/generate-sample.py --root .
python scripts/verify-public-content.py --root . --include-generated
git diff --check
```

On Windows, use `mvnw.cmd` when the Maven wrapper is present (for example,
`mvnw.cmd -B -ntp clean verify`). The Python
scripts are intended to work on Windows, Linux and macOS; the release shell
commands in GitHub Actions run only on the hosted Linux runner.

## Pull requests

Pull requests should include:

- the problem and the smallest public change that addresses it;
- affected modules, CLI/API contract and generated-project compatibility;
- tests for valid and invalid input, especially path and ZIP boundaries;
- the commands run and their result;
- security, dependency and license considerations;
- documentation updates in both languages when public behavior changes.

Do not claim a production or deployment result from a local build. Separate
source/build evidence from runtime or downstream evidence.

Keep commits small and descriptive. The preferred subject format is
`<type>(<scope>): <actual change>`, for example
`fix(core): reject parent traversal in ZIP entries`. Do not invent issue
numbers. Commits and pushes are not part of ordinary local implementation
work; repository maintainers will handle integration and release operations.

## Design and security expectations

The core generator must remain independent of web and database concerns. New
inputs must have explicit bounds and negative tests. Generated paths must be
normalized and confined to their intended temporary root. The public CLI and
server must not gain arbitrary filesystem or shell execution as a convenience
feature.

If a change requires a new third-party GitHub Action, pin it to a reviewed
commit SHA and explain the permission model. Fork pull requests must not see
repository secrets or use a privileged runner.

## Review and conduct

By participating, you agree to follow [CODE_OF_CONDUCT.md](CODE_OF_CONDUCT.md).
Security-sensitive reports must follow [SECURITY.md](SECURITY.md) rather than
being described in a public pull request.
