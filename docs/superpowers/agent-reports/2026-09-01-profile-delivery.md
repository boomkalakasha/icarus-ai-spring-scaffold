# Icarus Scaffold profile delivery report

Date: 2026-09-01
Scope: WP-03 / Task 3 follow-up — the minimum verifiable `simple`/`modular`
profile slice, honest no-AI entry documentation, and the existing trusted-
classpath TemplatePack boundary.

## Status

DONE_WITH_CONCERNS

No P0 or P1 issue was found in the implemented scope. The only validation
coverage concern is environmental: the Docker daemon was unavailable, so
container build/health evidence is explicitly `NOT_RUN`; no Docker result was
claimed as passing.

The candidate version remains 1.2.0 and is documented as Unreleased. No
commit, push, tag, release, external service, or new dependency was created.

## Design decisions

- `ScaffoldRequest.profile` accepts exactly `simple` and `modular`. Omitted or
  legacy constructor input resolves to `modular`, preserving the existing
  five-module output and byte-compatible default ZIP contract.
- The bundled trusted `default` TemplatePack supplies two manifests. `simple`
  renders one Maven module with the existing domain/application/infrastructure/
  API/boot package slice; `modular` retains the existing five-module shape.
  TemplatePack manifests remain classpath logical paths only, with duplicate
  and unsafe path rejection. No template directory, arbitrary filesystem path,
  URL, shell command, or runtime external JAR input was added.
- CLI `--profile` and REST `profile` reach the same core request. The CLI and
  REST documentation state that Scaffold does not include or host an AI model;
  explicit CLI/REST input is the reproducible no-AI entry point.
- The sample verifier now locates a packaged application JAR in either
  `boot/target` (modular) or root `target` (simple). The generated simple README
  does not interpolate the user-controlled description into a shell command.

## Changes

- Core request validation, rendering, TemplatePack profile manifests, and
  simple-profile classpath templates.
- CLI profile option and contract coverage; REST payload mapping and contract
  coverage.
- Core profile/default compatibility, manifest safety, and generated-shape
  tests.
- Bilingual README, CLI, REST, architecture, generated-project,
  troubleshooting, template README, and 1.2.0 Unreleased changelog updates.
- Python sample-verifier regression coverage for simple-profile root JARs.

## RED evidence

- `rtk mvn -B -ntp -pl icarus-scaffold-core -am '-Dtest=ScaffoldGeneratorContractTest,ScaffoldRequestValidatorTest' test`
  — exit 1 before implementation; the new profile constructor and
  `profile()` accessor were absent.
- `rtk python -B -m unittest scripts.test_release_documentation.ReleaseDocumentationTests.test_profiles_and_no_ai_reproducible_entry_are_documented -v`
  — exit 1 before documentation updates; the profile/no-AI markers were
  absent.
- `rtk python -B -m unittest scripts.test_generate_sample.PrepareOutputDirectoryTest.test_find_boot_jar_accepts_the_simple_profile_root_target -v`
  — exit 1 before the verifier fix; it only searched `boot/target`.
- A real simple-profile sample run initially built and tested the generated
  project successfully but exited 1 when the verifier could not find its root
  `target` JAR. This exposed the script compatibility defect above.

## GREEN and verification evidence

All commands were run from the Scaffold worktree.

- `rtk mvn -B -ntp -pl icarus-scaffold-core -am '-Dtest=ScaffoldGeneratorContractTest,ScaffoldRequestValidatorTest' test`
  — exit 0; 28 core tests passed, 0 failures/errors.
- `rtk mvn -B -ntp -pl icarus-scaffold-cli,icarus-scaffold-server -am '-Dtest=ScaffoldCliContractTest,ScaffoldControllerContractTest' '-Dsurefire.failIfNoSpecifiedTests=false' test`
  — exit 0; CLI contract tests and server contract tests passed (9 and 11
  respectively; the upstream core module also passed).
- `rtk python -B -m unittest scripts.test_release_documentation.ReleaseDocumentationTests.test_profiles_and_no_ai_reproducible_entry_are_documented -v`
  — exit 0; 1 test passed.
- `rtk python -B -m unittest scripts.test_generate_sample.PrepareOutputDirectoryTest.test_find_boot_jar_accepts_the_simple_profile_root_target -v`
  — exit 0; 1 test passed.
- `rtk mvn -B -ntp clean verify` — exit 0; core 50 tests, CLI 13 tests, and
  server 15 tests passed; 0 failures/errors. Maven emitted existing-style
  Surefire forked-JVM channel warnings and the Shade plugin's overlapping
  `META-INF/MANIFEST.MF` warning, but the reactor was successful.
- `rtk python -B -m unittest discover -s scripts -p "test_*.py"` — exit 0;
  27 tests passed, 1 skipped by environment.
- `rtk python scripts/generate-sample.py --root .` — exit 0; default modular
  sample package, generated tests, runtime health, and greeting checks passed;
  Docker daemon unavailable, therefore `NOT_RUN`.
- The equivalent simple-profile verifier run through `ICARUS_CLI_ARGS_JSON`
  — exit 0 after the root-target fix; one-module generated project package,
  four generated tests, runtime health, and greeting checks passed; Docker was
  again `NOT_RUN` because the daemon was unavailable.
- `rtk python scripts/verify-public-content.py --root . --include-generated`
  — exit 0; public-content scan passed.
- `rtk git diff --check` — exit 0.

## Unfinished items and risks

- Docker Compose/image/container-health validation remains `NOT_RUN` until a
  Docker daemon is available. This is an environment limitation, not a
  passing container claim.
- 1.2.0 release publication, immutable tag, remote assets, and release gates
  were intentionally not performed or asserted; the changelog remains
  `Unreleased`.
- Surefire forked-JVM channel warnings and Shade manifest-overlap warnings
  should be reviewed separately if the release gate treats warnings as
  blockers; they did not produce test failures in this run.
