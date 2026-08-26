#!/usr/bin/env python3
"""Scan public source and optional generated material for high-confidence risks.

This is a conservative pattern check, not a substitute for human review or a
secret manager. It reports filenames, line numbers and rule names only; it
never prints matching content. Extra repository-specific patterns can be
passed with --extra-pattern or PUBLIC_SCAN_EXTRA_PATTERNS.
"""

from __future__ import annotations

import argparse
from dataclasses import dataclass
import os
from pathlib import Path
import re
import subprocess
import sys


@dataclass(frozen=True)
class Finding:
    source: str
    location: str
    rule: str


SECRET_PATTERNS: tuple[tuple[str, re.Pattern[str]], ...] = (
    ("private-key", re.compile(r"-----BEGIN [A-Z0-9 ]*PRIVATE KEY-----")),
    ("aws-access-key", re.compile(r"\bAKIA[0-9A-Z]{16}\b")),
    (
        "github-token",
        re.compile(r"\b(?:gh[pousr]_[A-Za-z0-9_]{20,}|github_pat_[A-Za-z0-9_]{20,})\b"),
    ),
    (
        "bearer-token",
        re.compile(r"(?i)\bBearer\s+[A-Za-z0-9._~+/=-]{24,}\b"),
    ),
    (
        "credential-assignment",
        re.compile(
            r"(?i)\b(?:password|passwd|secret|api[-_]?key|access[-_]?token)\b"
            r"\s*[:=]\s*[\"'][^\"'\r\n]{12,}[\"']"
        ),
    ),
)

PRIVACY_PATTERNS: tuple[tuple[str, re.Pattern[str]], ...] = (
    ("private-dependency-marker", re.compile(r"(?i)\b(?:com\.metateam|e-iceblue|spire|exlibs)\b")),
    ("legacy-organization-marker", re.compile(r"(?i)\b(?:ysstech|yss-ai|biggroup)\b")),
    (
        "private-ipv4",
        re.compile(
            r"\b(?:"
            r"10\.(?:25[0-5]|2[0-4]\d|1?\d?\d)\.(?:25[0-5]|2[0-4]\d|1?\d?\d)\.(?:25[0-5]|2[0-4]\d|1?\d?\d)"
            r"|192\.168\.(?:25[0-5]|2[0-4]\d|1?\d?\d)\.(?:25[0-5]|2[0-4]\d|1?\d?\d)"
            r"|172\.(?:1[6-9]|2\d|3[0-1])\.(?:25[0-5]|2[0-4]\d|1?\d?\d)\.(?:25[0-5]|2[0-4]\d|1?\d?\d)"
            r")\b"
        ),
    ),
    ("private-domain", re.compile(r"(?i)\b[a-z0-9.-]+\.(?:internal|corp|lan)\b")),
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument(
        "--include-generated",
        action="store_true",
        help="Include .ci generated samples while still ignoring compiled target directories.",
    )
    parser.add_argument(
        "--history",
        action="store_true",
        help="Scan patch text from reachable Git history as well as the current tree.",
    )
    parser.add_argument(
        "--extra-pattern",
        action="append",
        default=[],
        help="Additional case-insensitive regular expression to reject (repeatable).",
    )
    return parser.parse_args()


def should_skip(path: Path, root: Path, include_generated: bool) -> bool:
    relative = path.relative_to(root)
    parts = relative.parts
    if path.name == "verify-public-content.py":
        return True
    if any(part in {".git", ".idea", ".vscode", "__pycache__", ".venv", "node_modules"} for part in parts):
        return True
    if "target" in parts:
        return True
    if not include_generated and ".ci" in parts:
        return True
    return False


def read_text(path: Path) -> str | None:
    try:
        data = path.read_bytes()
    except OSError:
        return None
    if b"\x00" in data:
        return None
    try:
        return data.decode("utf-8")
    except UnicodeDecodeError:
        return None


def scan_text(
    text: str,
    source: str,
    location_prefix: str,
    patterns: tuple[tuple[str, re.Pattern[str]], ...],
) -> list[Finding]:
    findings: list[Finding] = []
    lines = text.splitlines()
    for line_number, line in enumerate(lines, start=1):
        for rule, pattern in patterns:
            # Contract tests may intentionally mention a forbidden marker in
            # a negative assertion that proves it is absent from generated
            # output. Keep the assertion while avoiding a false positive.
            if (
                (location_prefix.endswith("Test.java") or source == "history")
                and rule.endswith("marker")
                and "assertFalse" in line
            ):
                continue
            if pattern.search(line):
                findings.append(Finding(source, f"{location_prefix}:{line_number}", rule))
    return findings


def scan_tree(
    root: Path,
    include_generated: bool,
    patterns: tuple[tuple[str, re.Pattern[str]], ...],
) -> list[Finding]:
    findings: list[Finding] = []
    for directory, directory_names, file_names in os.walk(root):
        directory_path = Path(directory)
        directory_names[:] = [
            name
            for name in directory_names
            if not should_skip(directory_path / name, root, include_generated)
        ]
        for name in file_names:
            path = directory_path / name
            if should_skip(path, root, include_generated):
                continue
            text = read_text(path)
            if text is None:
                continue
            relative = path.relative_to(root).as_posix()
            findings.extend(scan_text(text, "tree", relative, patterns))
    return findings


def scan_history(root: Path, patterns: tuple[tuple[str, re.Pattern[str]], ...]) -> list[Finding]:
    completed = subprocess.run(
        [
            "git",
            "log",
            "--all",
            "-p",
            "--no-ext-diff",
            "--text",
            "--",
            ".",
            ":(exclude)scripts/verify-public-content.py",
        ],
        cwd=root,
        check=False,
        stdout=subprocess.PIPE,
        stderr=subprocess.PIPE,
        text=True,
        encoding="utf-8",
        errors="replace",
    )
    if completed.returncode != 0:
        return []
    return scan_text(completed.stdout, "history", "git-log", patterns)


def main() -> int:
    options = parse_args()
    root = options.root.resolve()
    if not root.is_dir():
        print(f"ERROR: repository root does not exist: {root}", file=sys.stderr)
        return 1

    extra_values = list(options.extra_pattern)
    if os.environ.get("PUBLIC_SCAN_EXTRA_PATTERNS"):
        extra_values.extend(
            value for value in os.environ["PUBLIC_SCAN_EXTRA_PATTERNS"].splitlines() if value
        )
    extra_patterns: list[tuple[str, re.Pattern[str]]] = []
    for index, value in enumerate(extra_values, start=1):
        try:
            extra_patterns.append((f"extra-{index}", re.compile(value, re.IGNORECASE)))
        except re.error as error:
            print(f"ERROR: invalid extra pattern {index}: {error}", file=sys.stderr)
            return 1

    patterns = SECRET_PATTERNS + PRIVACY_PATTERNS + tuple(extra_patterns)
    findings = scan_tree(root, options.include_generated, patterns)
    if options.history:
        findings.extend(scan_history(root, patterns))

    if findings:
        print("Public-content scan failed:", file=sys.stderr)
        for finding in findings[:200]:
            print(
                f"- {finding.source}:{finding.location}: rule={finding.rule}",
                file=sys.stderr,
            )
        if len(findings) > 200:
            print(f"- ... {len(findings) - 200} more findings omitted", file=sys.stderr)
        return 1

    scope = "tree and generated material" if options.include_generated else "tree"
    if options.history:
        scope += "; history"
    print(f"Public-content scan passed ({scope}).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
