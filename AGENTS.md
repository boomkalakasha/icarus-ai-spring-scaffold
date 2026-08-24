# Repository guidance

This repository is the public source of `icarus-ai-spring-scaffold`, a Java 17
Spring Boot project generator. Treat the public repository as the source of
truth for reusable functionality; private downstream configuration, customer
data, deployment credentials and organization-specific automation do not
belong here.

## Delivery profile

- Use the `github-open-source` delivery profile.
- `main` is the protected release branch. Changes arrive through reviewed pull
  requests and must not be pushed directly to `main`.
- Use SemVer tags in the form `vMAJOR.MINOR.PATCH`. A tag is immutable once
  released; `v1.0.0` is the initial stable release line.
- Do not commit or push as part of local implementation work unless that
  operation is explicitly requested and separately authorized.

## Public-safety rules

- Keep source, templates, examples and documentation free of credentials,
  private hostnames, private dependencies, customer identifiers and copied
  internal history.
- Do not add arbitrary output paths, shell execution, template-directory
  overrides or overwrite behavior to the generator without a documented
  security review and tests.
- Keep GitHub Actions pinned to reviewed commit SHAs. Do not use
  `pull_request_target` to check out or execute pull-request code.
- Never put secrets in workflows, issue templates, sample configuration or
  generated projects. Fork pull requests must run with read-only permissions
  and without repository secrets.

## Verification

From the repository root, use Java 17 and Maven:

```text
mvn -B -ntp clean verify
python scripts/test_generate_sample.py
python scripts/generate-sample.py --root .
python scripts/verify-public-content.py --root . --include-generated
```

If the Maven wrapper is present, prefer `./mvnw` on Unix-like systems and
`mvnw.cmd` on Windows. The sample-generation script detects the wrapper and
the platform automatically. A generated project is a verification artifact,
not a promise that every downstream deployment environment is supported.

Before opening a pull request, also check `git diff --check`, review the
generated ZIP contents, and confirm that release notes describe only evidence
available from the current tag.

## Change boundaries

Changes that affect generator inputs, ZIP paths, template rendering, CLI
output, or the optional server must include negative tests for invalid input
and a generated-sample build. Do not silently change the public contract of
the generated project. Keep bilingual user-facing documentation in sync when
behavior or security boundaries change.
