import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


class ReleaseDocumentationTests(unittest.TestCase):
    def test_readmes_use_theme_compatible_watermark(self):
        for name in ("README.md", "README.zh-CN.md"):
            source = (ROOT / name).read_text(encoding="utf-8")
            self.assertIn("docs/assets/brand/watermark-auto.svg", source, name)

        automatic = ROOT / "docs" / "assets" / "brand" / "watermark-auto.svg"
        explicit_light_surface = ROOT / "docs" / "assets" / "brand" / "watermark-dark.svg"
        self.assertTrue(automatic.is_file())
        auto_source = automatic.read_text(encoding="utf-8")
        self.assertIn("@media (prefers-color-scheme: dark)", auto_source)
        self.assertRegex(auto_source, r'<text[^>]*class="wordmark"[^>]*stroke-width="3"')
        self.assertRegex(explicit_light_surface.read_text(encoding="utf-8"), r'<text[^>]*stroke="#F7F4EC"[^>]*stroke-width="3"')

    def test_v112_is_described_as_the_public_release_with_evidence_boundaries(self):
        english = (ROOT / "README.md").read_text(encoding="utf-8")
        chinese = (ROOT / "README.zh-CN.md").read_text(encoding="utf-8")
        changelog = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")

        self.assertIn("public stable release is [`v1.1.2`]", english)
        self.assertIn("[`v1.1.2`]", chinese)
        self.assertIn("checksums, SBOM, and build provenance", english)
        self.assertIn("校验和、SBOM 与构建来源证明", chinese)
        self.assertIn("## [1.1.2] - 2026-08-26", changelog)
        self.assertIn("## [1.1.1] - 2026-08-26", changelog)
        self.assertIn("## [1.1.0] - 2026-08-26", changelog)

    def test_runtime_troubleshooting_describes_the_dynamic_local_port_truthfully(self):
        troubleshooting = (ROOT / "docs" / "troubleshooting.md").read_text(encoding="utf-8")
        self.assertIn("bounded free local port", troubleshooting)
        self.assertNotIn("18080 by default", troubleshooting)

    def test_readmes_lead_with_bilingual_value_proposition_and_companions(self):
        english = (ROOT / "README.md").read_text(encoding="utf-8")
        chinese = (ROOT / "README.zh-CN.md").read_text(encoding="utf-8")
        self.assertIn("从一句需求，生成一套可审查的服务骨架。", english)
        self.assertIn("From one idea to a reviewable service skeleton.", english)
        self.assertIn("CLI or REST adapter", english)
        self.assertIn("https://github.com/boomkalakasha/ai-first-vibe-coding-skill", english)
        self.assertIn("https://github.com/boomkalakasha/icarus-open-source-governance-skill", english)
        self.assertIn("从一句需求，生成一套可审查的服务骨架。", chinese)
        self.assertIn("作为团队或协作 Agent 的安全起点", chinese)
        self.assertIn("https://github.com/boomkalakasha/ai-first-vibe-coding-skill", chinese)
        self.assertIn("https://github.com/boomkalakasha/icarus-open-source-governance-skill", chinese)

    def test_v111_changelog_records_direct_start_port_support(self):
        changelog = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")
        self.assertIn("## [1.1.1] - 2026-08-26", changelog)
        self.assertIn("requested application port", changelog)


if __name__ == "__main__":
    unittest.main()
