# Security policy

## Supported versions

Security fixes are considered for the latest patch release on the current
stable major/minor line. The initial supported line is:

| Version | Supported |
| --- | --- |
| 1.0.x | Yes |
| Older or unreleased snapshots | No guarantee |

Support status is about the public source release. It does not certify a
generated application's deployment or security posture.

## Reporting a vulnerability

Please use GitHub's private vulnerability reporting or a private security
advisory for this repository when available. If private reporting is not
enabled, open a minimal public issue containing no exploit details and ask the
maintainers for a private reporting channel.

Include only the information needed to reproduce the issue safely:

- affected release, commit or generated-project inputs;
- expected and observed behavior;
- a minimal reproduction or test case, with secrets removed;
- impact assessment and any safe mitigation.

Do not include credentials, private URLs, customer information, live tokens or
an exploit that affects an unpatched public release. Do not send sensitive
material through a public pull request.

Maintainers will acknowledge a report when practicable, triage its severity,
and coordinate a fix or mitigation. No response time, patch time or disclosure
date is guaranteed; coordinated disclosure will be agreed with the reporter
when appropriate.

## Scope notes

The repository generates source projects. A vulnerability in a downstream
generated application's own code or deployment should first be reproduced
against the generator version and template input. Please distinguish a
generator defect from a downstream configuration issue in the report.
