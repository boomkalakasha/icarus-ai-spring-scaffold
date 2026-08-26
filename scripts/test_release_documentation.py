import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


class ReleaseDocumentationTests(unittest.TestCase):
    def test_v110_is_described_as_the_public_release_with_evidence_boundaries(self):
        english = (ROOT / "README.md").read_text(encoding="utf-8")
        chinese = (ROOT / "README.zh-CN.md").read_text(encoding="utf-8")
        changelog = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")

        self.assertIn("latest\npublic stable release", english)
        self.assertIn("最新公开稳定版本", chinese)
        self.assertIn("checksums, SBOM, and build provenance", english)
        self.assertIn("校验和、SBOM 与构建来源证明", chinese)
        self.assertIn("## [Unreleased]", changelog)
        self.assertIn("## [1.1.0] - 2026-08-26", changelog)

    def test_runtime_troubleshooting_describes_the_dynamic_local_port_truthfully(self):
        troubleshooting = (ROOT / "docs" / "troubleshooting.md").read_text(encoding="utf-8")
        self.assertIn("bounded free local port", troubleshooting)
        self.assertNotIn("18080 by default", troubleshooting)


if __name__ == "__main__":
    unittest.main()
