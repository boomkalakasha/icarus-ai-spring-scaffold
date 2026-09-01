#!/usr/bin/env python3
"""Regression tests for destructive-path safeguards in generate-sample.py."""

from __future__ import annotations

import importlib.util
import json
import os
from pathlib import Path
import shutil
import subprocess
import tempfile
import unittest
from unittest import mock


MODULE_PATH = Path(__file__).with_name("generate-sample.py")
SPEC = importlib.util.spec_from_file_location("generate_sample", MODULE_PATH)
if SPEC is None or SPEC.loader is None:
    raise RuntimeError("could not load generate-sample.py")
GENERATE_SAMPLE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(GENERATE_SAMPLE)


class PrepareOutputDirectoryTest(unittest.TestCase):

    def test_run_timeout_terminates_the_entire_process_tree(self) -> None:
        calls = []

        class Process:
            pid = 777

            def communicate(self, timeout=None):
                calls.append(("communicate", timeout))
                raise subprocess.TimeoutExpired(["docker", "build"], timeout)

            def poll(self):
                return None

            def wait(self, timeout=None):
                calls.append(("wait", timeout))
                return -9

        process = Process()
        with (
            mock.patch.object(GENERATE_SAMPLE.os, "name", "nt"),
            mock.patch.object(GENERATE_SAMPLE.subprocess, "Popen", return_value=process),
            mock.patch.object(
                GENERATE_SAMPLE.subprocess,
                "run",
                return_value=subprocess.CompletedProcess(["taskkill"], 0, "", ""),
            ) as taskkill,
        ):
            with self.assertRaisesRegex(GENERATE_SAMPLE.VerificationError, "timed out after 1s"):
                GENERATE_SAMPLE.run(["docker", "build"], Path("."), timeout=1)

        taskkill.assert_called_once()
        self.assertEqual(taskkill.call_args.args[0], ["taskkill", "/PID", "777", "/T", "/F"])
        self.assertIn(("wait", 5), calls)

    def test_docker_check_timeout_allows_cold_image_pulls_and_safe_override(self) -> None:
        self.assertGreaterEqual(GENERATE_SAMPLE.DEFAULT_DOCKER_CHECK_TIMEOUT_SECONDS, 600)
        with mock.patch.dict(GENERATE_SAMPLE.os.environ, {"ICARUS_DOCKER_CHECK_TIMEOUT_SECONDS": "12.5"}):
            self.assertEqual(12.5, GENERATE_SAMPLE.docker_check_timeout_seconds())

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

    def test_runtime_smoke_uses_the_generated_port_without_a_command_line_override(self) -> None:
        class Process:
            pid = 123
            returncode = 0
            terminated = False

            def poll(self):
                return None

            def terminate(self):
                self.terminated = True

            def wait(self, timeout=None):
                return 0

        with tempfile.TemporaryDirectory() as temporary:
            output_dir = Path(temporary)
            jar = output_dir / "boot.jar"
            captured = []
            process = Process()

            def start(command, **kwargs):
                captured.extend(command)
                return process

            with (
                mock.patch.object(GENERATE_SAMPLE, "find_boot_jar", return_value=jar),
                mock.patch.object(GENERATE_SAMPLE.os, "name", "posix"),
                mock.patch.object(GENERATE_SAMPLE.subprocess, "Popen", side_effect=start),
                mock.patch.object(GENERATE_SAMPLE.subprocess, "run"),
                mock.patch.object(
                    GENERATE_SAMPLE,
                    "fetch_json",
                    side_effect=[
                        (200, {"status": "UP"}),
                        (200, {"subject": "sample", "message": "Hello, sample!"}),
                    ],
                ),
            ):
                GENERATE_SAMPLE.run_runtime_smoke(output_dir, "java", 18080, output_dir)

        self.assertEqual(captured, ["java", "-jar", str(jar)])
        self.assertTrue(process.terminated)

    def test_find_boot_jar_accepts_the_simple_profile_root_target(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            project_root = Path(temporary)
            target = project_root / "target"
            target.mkdir()
            jar = target / "demo-service-0.1.0-SNAPSHOT.jar"
            jar.write_bytes(b"jar")

            self.assertEqual(jar, GENERATE_SAMPLE.find_boot_jar(project_root))

    def test_docker_cleanup_removes_the_compose_owned_local_image(self) -> None:
        commands = []
        cleanup = []

        def fake_run(command, cwd, **kwargs):
            commands.append(command)
            stdout = "container-id\n" if "ps" in command else "healthy\n" if "inspect" in command else ""
            return subprocess.CompletedProcess(command, 0, stdout, "")

        def fake_probe(command, cwd, timeout):
            cleanup.append(command)
            return subprocess.CompletedProcess(command, 0, "", "")

        with tempfile.TemporaryDirectory() as temporary:
            with (
                mock.patch.object(GENERATE_SAMPLE, "docker_command", return_value=("docker", "available")),
                mock.patch.object(GENERATE_SAMPLE.os, "getpid", return_value=4242),
                mock.patch.object(GENERATE_SAMPLE, "run", side_effect=fake_run),
                mock.patch.object(GENERATE_SAMPLE, "probe", side_effect=fake_probe),
            ):
                status = GENERATE_SAMPLE.run_docker_checks(Path(temporary), Path(temporary))

        self.assertIn("healthy container", status)
        compose_prefix = ["docker", "compose", "--project-name", "icarus-scaffold-sample-4242", "-f", "compose.yaml"]
        self.assertTrue(all(command[:6] == compose_prefix for command in commands if command[:2] == ["docker", "compose"]))
        self.assertIn(compose_prefix + ["down", "--volumes", "--remove-orphans", "--rmi", "local", "--timeout", "10"], cleanup)


if __name__ == "__main__":
    unittest.main()
