# Support

Use public issues for reproducible defects and feature proposals. Use a
discussion or a question issue for usage help when that facility is enabled in
the repository. Security vulnerabilities and sensitive data do not belong in
public support channels; follow [SECURITY.md](SECURITY.md).

## What to include

Please provide:

- the project release or commit;
- operating system, JDK and Maven versions;
- the exact generator command with secrets and private values replaced;
- the smallest input that reproduces the behavior;
- relevant output, stack trace or ZIP listing;
- whether the failure is in the generator, generated source build, or a
  downstream runtime.

Do not paste access tokens, credentials, private hostnames, customer data or
unredacted environment files. A local build or generated sample is evidence of
that environment only; it does not establish support for every deployment
platform.

## Before opening a report

```bash
mvn -B -ntp clean verify
python scripts/generate-sample.py --root .
python scripts/verify-public-content.py --root . --include-generated
```

Search existing issues first and include links to related reports. If the
problem is a security issue, stop and use the private process in
[SECURITY.md](SECURITY.md).
