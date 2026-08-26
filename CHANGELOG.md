# Changelog

All notable public changes to this project are documented here. The format is
based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and releases
follow [Semantic Versioning](https://semver.org/).

## [Unreleased]

No unreleased changes are recorded yet.

## [1.1.0] - 2026-08-26

The productization release adds:

- a cwd-confined `--output <filename.zip>` CLI option with no-overwrite,
  race-safe `CREATE_NEW` semantics while preserving stdout as the default;
- generated-sample package, bounded health/greeting runtime checks and optional
  Docker Compose capability checks with explicit `NOT_RUN` when Docker is not
  available;
- a JRE-only generated container health probe, public LICENSE/SUPPORT files,
  and bilingual quick-start and troubleshooting documentation;
- local public brand asset copies and version-aligned release documentation.

## [1.0.0] - 2026-08-25

Initial public release line:

- security-focused Spring project generation with validated coordinates and
  confined template paths;
- core, CLI and optional server boundaries;
- generated-project tests and repository-level sample-build verification;
- bilingual documentation and public contribution/security guidance;
- GitHub CI, dependency automation, static security analysis, checksums, SBOM
  and build-provenance release metadata.
