#!/usr/bin/env python3
"""Regression tests for destructive-path safeguards in generate-sample.py."""

from __future__ import annotations

import importlib.util
import json
import os
from pathlib import Path
import shutil
import tempfile
import unittest


MODULE_PATH = Path(__file__).with_name("generate-sample.py")
SPEC = importlib.util.spec_from_file_location("generate_sample", MODULE_PATH)
if SPEC is None or SPEC.loader is None:
    raise RuntimeError("could not load generate-sample.py")
GENERATE_SAMPLE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(GENERATE_SAMPLE)


class PrepareOutputDirectoryTest(unittest.TestCase):

    def test_sample_verification_contract_covers_package_runtime_and_docker_states(self) -> None:
        source = MODULE_PATH.read_text(encoding="utf-8").lower()
        for marker in (
            "package",
            "actuator/health",
            "/api/greetings",
            "not_run",
            "docker compose",
            "docker build",
            "docker compose down",
        ):
            self.assertIn(marker, source, marker)

    def test_custom_cli_arguments_cannot_escape_stdout_capture(self) -> None:
        original = os.environ.get("ICARUS_CLI_ARGS_JSON")
        try:
            os.environ["ICARUS_CLI_ARGS_JSON"] = json.dumps(["--output", "outside.zip"])
            with self.assertRaises(GENERATE_SAMPLE.VerificationError):
                GENERATE_SAMPLE.cli_args()
        finally:
            if original is None:
                os.environ.pop("ICARUS_CLI_ARGS_JSON", None)
            else:
                os.environ["ICARUS_CLI_ARGS_JSON"] = original

    def test_default_output_uses_a_safe_temporary_directory(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary).resolve()
            output = GENERATE_SAMPLE.prepare_output_dir(root, None)
            try:
                self.assertNotEqual(root, output)
                self.assertFalse(output.is_relative_to(root))
                self.assertTrue((output / ".generated-by-icarus-scaffold").is_file())
            finally:
                shutil.rmtree(output)

    def test_repository_root_is_never_a_deletion_target(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary).resolve()
            marker = root / ".generated-by-icarus-scaffold"
            marker.write_text("forged marker\n", encoding="utf-8")

            with self.assertRaises(GENERATE_SAMPLE.VerificationError):
                GENERATE_SAMPLE.prepare_output_dir(root, Path("."))

            self.assertTrue(marker.is_file())

    def test_ci_root_is_never_a_deletion_target(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary).resolve()
            ci_root = root / ".ci"
            ci_root.mkdir()
            marker = ci_root / ".generated-by-icarus-scaffold"
            marker.write_text("forged marker\n", encoding="utf-8")

            with self.assertRaises(GENERATE_SAMPLE.VerificationError):
                GENERATE_SAMPLE.prepare_output_dir(root, Path(".ci"))

            self.assertTrue(marker.is_file())

    def test_marked_child_of_ci_can_be_recreated(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary).resolve()
            output = root / ".ci" / "generated-sample"
            output.mkdir(parents=True)
            (output / ".generated-by-icarus-scaffold").write_text("old\n", encoding="utf-8")
            (output / "old.txt").write_text("old\n", encoding="utf-8")

            recreated = GENERATE_SAMPLE.prepare_output_dir(root, Path(".ci/generated-sample"))

            self.assertEqual(output, recreated)
            self.assertFalse((output / "old.txt").exists())
            self.assertTrue((output / ".generated-by-icarus-scaffold").is_file())

    def test_symbolic_linked_ci_directory_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as temporary, tempfile.TemporaryDirectory() as external:
            root = Path(temporary).resolve()
            try:
                os.symlink(Path(external).resolve(), root / ".ci", target_is_directory=True)
            except OSError as error:
                self.skipTest(f"symbolic links are unavailable: {error}")

            with self.assertRaises(GENERATE_SAMPLE.VerificationError):
                GENERATE_SAMPLE.prepare_output_dir(root, Path(".ci/generated-sample"))


if __name__ == "__main__":
    unittest.main()
