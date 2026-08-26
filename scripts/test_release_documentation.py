import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


class ReleaseDocumentationTests(unittest.TestCase):
    def test_v110_is_described_as_a_local_candidate_until_remote_gates_exist(self):
        english = (ROOT / "README.md").read_text(encoding="utf-8")
        chinese = (ROOT / "README.zh-CN.md").read_text(encoding="utf-8")
        changelog = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")

        self.assertIn("feature branch is a\nlocal candidate", english)
        self.assertIn("功能分支是本地候选", chinese)
        self.assertIn("## [Unreleased]", changelog)
        self.assertNotIn("current release is `v1.1.0`", english)

    def test_runtime_troubleshooting_describes_the_dynamic_local_port_truthfully(self):
        troubleshooting = (ROOT / "docs" / "troubleshooting.md").read_text(encoding="utf-8")
        self.assertIn("bounded free local port", troubleshooting)
        self.assertNotIn("18080 by default", troubleshooting)


if __name__ == "__main__":
    unittest.main()
