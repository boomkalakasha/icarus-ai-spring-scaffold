# Changelog

All notable public changes to this project are documented here. The format is
based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and releases
follow [Semantic Versioning](https://semver.org/).

## [1.1.4] - 2026-08-28

> Candidate notes for the next release; this version is not public until its
> tag, CI, assets, and release gates are independently verified.

### Fixed

- Stopped generated projects from inheriting an Icarus copyright or LICENSE by
  default; a LICENSE is now emitted only when the caller supplies a supported
  license, rights holder, and year as one complete decision.
- Added validated Apache-2.0 and MIT rendering across the core, CLI and REST
  adapters, including negative tests for partial or unsupported license input.

### Changed

- Reworked the bilingual first-glance description around a consistent,
  reviewable, buildable and testable Java service starting point.
- Replaced hard-coded latest-version claims with dynamic GitHub Release facts
  and added an illustrative generated project tree and runnable API result.
- Added the reusable Governance release-documentation gate, pinned to the
  reviewed `main` commit SHA, before release packaging.

## [1.1.3] - 2026-08-26

> Candidate notes for the next release; this version is not public until its
> tag, CI, assets, and release gates are independently verified.

### Fixed

- Terminate timed-out Docker/Compose process trees on Windows so Buildx cannot
  keep the generated temporary directory locked after a failed verification.
- Allow a 600-second cold-cache Docker build window by default, with a positive
  `ICARUS_DOCKER_CHECK_TIMEOUT_SECONDS` override for slower or faster runners.
- Reworked the English and Chinese READMEs around a core-feature table, common
  use cases and a four-step first-project path so new users can reach a useful
  generated sample without reconstructing the workflow from scattered sections.

## [1.1.2] - 2026-08-26

### Added

- Added a first-glance bilingual value proposition and a concrete new-service generation scenario.
- Added companion links to AI-first Vibe Coding Skill and Icarus Open-source Governance.

## [1.1.1] - 2026-08-26

### Fixed

- Generated projects now honor the requested application port during direct `java -jar` startup; the sample verifier exercises that generated default instead of overriding it on the command line.
- Generated-sample cleanup now uses a unique Compose project name and removes only its own local images, temporary containers, networks, and verification directory.
- Recorded the theme-compatible watermark and published-release wording changes that are present on `main` but not in the immutable `v1.1.0` tag.

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

[1.1.4]: https://github.com/boomkalakasha/icarus-ai-spring-scaffold/releases/tag/v1.1.4
[1.1.3]: https://github.com/boomkalakasha/icarus-ai-spring-scaffold/releases/tag/v1.1.3
[1.1.2]: https://github.com/boomkalakasha/icarus-ai-spring-scaffold/releases/tag/v1.1.2
[1.1.1]: https://github.com/boomkalakasha/icarus-ai-spring-scaffold/releases/tag/v1.1.1
[1.1.0]: https://github.com/boomkalakasha/icarus-ai-spring-scaffold/releases/tag/v1.1.0
[1.0.0]: https://github.com/boomkalakasha/icarus-ai-spring-scaffold/releases/tag/v1.0.0
