## Summary

<!-- What problem does this change solve? Keep the scope public and focused. -->

## Verification

- [ ] `mvn -B -ntp clean verify`
- [ ] `python scripts/generate-sample.py --root .`
- [ ] `python scripts/verify-public-content.py --root . --include-generated`
- [ ] `git diff --check`

## Review checklist

- [ ] Invalid inputs and security boundaries have tests where applicable.
- [ ] Generated ZIP paths and generated-project build behavior were checked.
- [ ] No credentials, private hostnames, customer identifiers, private
      dependencies or copied private history are included.
- [ ] New GitHub Actions are pinned to reviewed commit SHAs and use the
      smallest required permissions.
- [ ] Public documentation is updated in both languages when behavior changes.
- [ ] Claims are limited to evidence from this change; no deployment or
      production result is implied by a local build.

## Compatibility and release impact

<!-- Mention CLI/API/template compatibility, migration notes, or say None. -->
