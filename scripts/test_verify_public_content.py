#!/usr/bin/env python3
"""Regression tests for the public-content scanner."""

from __future__ import annotations

import importlib.util
from pathlib import Path
import sys
import unittest


MODULE_PATH = Path(__file__).with_name("verify-public-content.py")
SPEC = importlib.util.spec_from_file_location("verify_public_content", MODULE_PATH)
if SPEC is None or SPEC.loader is None:
    raise RuntimeError("could not load verify-public-content.py")
SCANNER = importlib.util.module_from_spec(SPEC)
sys.modules[SPEC.name] = SCANNER
SPEC.loader.exec_module(SCANNER)


class HistoryScannerTest(unittest.TestCase):

    def test_negative_assertion_does_not_report_forbidden_marker(self) -> None:
        findings = SCANNER.scan_text(
            '+        assertFalse(allText.contains("S' + 'pire"));',
            "history",
            "git-log",
            SCANNER.PRIVACY_PATTERNS,
        )

        self.assertEqual([], findings)

    def test_real_forbidden_marker_is_still_reported(self) -> None:
        findings = SCANNER.scan_text(
            "+runtime dependency: " + "S" + "pire",
            "history",
            "git-log",
            SCANNER.PRIVACY_PATTERNS,
        )

        self.assertEqual(1, len(findings))
        self.assertEqual("private-dependency-marker", findings[0].rule)

    def test_private_ipv4_rule_does_not_mistake_a_tomcat_version_for_an_address(self) -> None:
        findings = SCANNER.scan_text(
            "Apache Tomcat/10.1.44",
            "tree",
            "runtime.log",
            SCANNER.PRIVACY_PATTERNS,
        )

        self.assertEqual([], findings)

    def test_private_ipv4_rule_reports_a_complete_rfc1918_address(self) -> None:
        findings = SCANNER.scan_text(
            "endpoint 10." + "42.0.5",
            "tree",
            "settings.txt",
            SCANNER.PRIVACY_PATTERNS,
        )

        self.assertEqual(1, len(findings))
        self.assertEqual("private-ipv4", findings[0].rule)


if __name__ == "__main__":
    unittest.main()
