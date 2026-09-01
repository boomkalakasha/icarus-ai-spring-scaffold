import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]


class ReleaseDocumentationTests(unittest.TestCase):
    def test_root_agent_guide_records_template_pack_and_module_guidance_boundaries(self):
        guide = (ROOT / "AGENTS.md").read_text(encoding="utf-8")
        for marker in (
            "template pack",
            "project-level guide",
            "module-level",
            "NOT_NEEDED",
            "mvn -B -ntp clean verify",
        ):
            self.assertIn(marker, guide, marker)

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

    def test_readmes_use_dynamic_release_facts_and_show_a_generated_outcome(self):
        english = (ROOT / "README.md").read_text(encoding="utf-8")
        chinese = (ROOT / "README.zh-CN.md").read_text(encoding="utf-8")
        changelog = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")

        for source in (english, chinese):
            self.assertIn("<!-- icarus-release-fact: dynamic -->", source)
            self.assertIn("https://github.com/boomkalakasha/icarus-ai-spring-scaffold/releases/latest", source)
            self.assertIn("https://github.com/boomkalakasha/icarus-ai-spring-scaffold/releases", source)
        self.assertNotIn("latest public stable release is", english.lower())
        self.assertNotIn("最新公开稳定版是", chinese)
        self.assertLess(english.index("## What you get"), english.index("## What is here"))
        self.assertIn("Illustrative generated result", english)
        self.assertIn("GET /api/greetings?subject=team", english)
        self.assertIn('{"subject":"team","message":"Hello, team!"}', english)
        self.assertLess(chinese.index("## 你会得到什么"), chinese.index("## 仓库内容"))
        self.assertIn("示意生成结果", chinese)
        self.assertIn('{"subject":"team","message":"Hello, team!"}', chinese)
        self.assertIn("Candidate notes for the next release", changelog)
        self.assertIn("checksums, SBOM, and build provenance", english)
        self.assertIn("校验和、SBOM 与构建来源证明", chinese)
        self.assertIn("## [1.2.0] - Unreleased", changelog)
        self.assertIn("## [1.1.4] - 2026-08-28", changelog)
        self.assertIn("## [1.1.2] - 2026-08-26", changelog)
        self.assertIn("## [1.1.1] - 2026-08-26", changelog)
        self.assertIn("## [1.1.0] - 2026-08-26", changelog)

        workflow = (ROOT / ".github" / "workflows" / "release.yml").read_text(encoding="utf-8")
        self.assertIn(
            "icarus-open-source-governance-skill/actions/release-doc-sync@"
            "12999d05ccc73800b5d6c49b709e2f09e8303519",
            workflow,
        )
        self.assertIn("steps.release-metadata.outputs.version", workflow)

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
        self.assertIn("可审查、可构建、可测试的起点", chinese)
        self.assertIn("https://github.com/boomkalakasha/ai-first-vibe-coding-skill", chinese)
        self.assertIn("https://github.com/boomkalakasha/icarus-open-source-governance-skill", chinese)

    def test_readmes_explain_core_features_and_first_project_path(self):
        english = (ROOT / "README.md").read_text(encoding="utf-8")
        chinese = (ROOT / "README.zh-CN.md").read_text(encoding="utf-8")

        self.assertIn("## At a glance", english)
        self.assertIn("multi-module Spring Boot project ZIP", english)
        self.assertIn("## Quick start (60 seconds)", english)
        self.assertIn("Unpack it and read the generated `AGENTS.md`", english)
        self.assertIn("python scripts/generate-sample.py --root .", english)

        self.assertIn("## 一眼看懂：它能帮你做什么", chinese)
        self.assertIn("Spring Boot 多模块项目 ZIP", chinese)
        self.assertIn("## 60 秒快速开始", chinese)
        self.assertIn("解压后先阅读生成项目里的 `AGENTS.md`", chinese)
        self.assertIn("python scripts/generate-sample.py --root .", chinese)

    def test_readmes_make_the_first_install_and_entry_choice_explicit(self):
        english = (ROOT / "README.md").read_text(encoding="utf-8")
        chinese = (ROOT / "README.zh-CN.md").read_text(encoding="utf-8")
        for source, markers in (
            (english, ("No global Maven installation is required", "Generate locally or in CI", "optional REST adapter", "latest GitHub Release")),
            (chinese, ("不需要全局安装 Maven", "本地或 CI 生成项目", "可选 REST 适配器", "最新 GitHub Release")),
        ):
            for marker in markers:
                self.assertIn(marker, source, marker)

    def test_template_pack_boundary_is_documented_in_both_languages(self):
        english = (ROOT / "README.md").read_text(encoding="utf-8")
        chinese = (ROOT / "README.zh-CN.md").read_text(encoding="utf-8")
        architecture = (ROOT / "docs" / "architecture.md").read_text(encoding="utf-8")
        cli = (ROOT / "docs" / "cli.md").read_text(encoding="utf-8")
        rest = (ROOT / "docs" / "rest-api.md").read_text(encoding="utf-8")

        for source in (english, chinese, architecture, rest):
            self.assertIn("templatePack", source)
            self.assertIn("default", source)
        self.assertIn("--template-pack", cli)
        self.assertIn("default", cli)
        self.assertIn("TemplatePack", english)
        self.assertIn("TemplatePack", chinese)
        self.assertIn("icarus.scaffold.allowed-template-packs", rest)
        self.assertIn("trusted classpath", architecture)
        self.assertIn("classpath", cli)

    def test_profiles_and_no_ai_reproducible_entry_are_documented(self):
        english = (ROOT / "README.md").read_text(encoding="utf-8")
        chinese = (ROOT / "README.zh-CN.md").read_text(encoding="utf-8")
        cli = (ROOT / "docs" / "cli.md").read_text(encoding="utf-8")

        self.assertIn("--profile simple", english)
        self.assertIn("simple profile", english)
        self.assertIn("modular profile", english)
        self.assertIn("does not include or host an AI model", english)
        self.assertIn("--profile simple", chinese)
        self.assertIn("simple profile", chinese)
        self.assertIn("modular profile", chinese)
        self.assertIn("不内置或托管大模型", chinese)
        self.assertIn("--profile", cli)
        self.assertIn("simple", cli)
        self.assertIn("modular", cli)

    def test_scaffold_candidate_version_and_jar_names_are_consistent(self):
        poms = (
            ROOT / "pom.xml",
            ROOT / "icarus-scaffold-core" / "pom.xml",
            ROOT / "icarus-scaffold-cli" / "pom.xml",
            ROOT / "icarus-scaffold-server" / "pom.xml",
        )
        for pom in poms:
            self.assertIn("<version>1.2.0</version>", pom.read_text(encoding="utf-8"), str(pom))

        for name in ("README.md", "README.zh-CN.md", "docs/cli.md", "docs/rest-api.md", "docs/troubleshooting.md"):
            source = (ROOT / name).read_text(encoding="utf-8")
            self.assertIn("1.2.0", source, name)
            self.assertNotIn("icarus-scaffold-cli-1.1.4", source, name)
            self.assertNotIn("icarus-scaffold-server-1.1.4", source, name)

    def test_v113_changelog_records_timeout_cleanup(self):
        changelog = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")
        self.assertIn("process trees", changelog)
        self.assertIn("ICARUS_DOCKER_CHECK_TIMEOUT_SECONDS", changelog)
        for version, next_version in (("1.1.4", "1.1.3"), ("1.1.3", "1.1.2")):
            section = changelog.split(f"## [{version}]", 1)[1].split(f"## [{next_version}]", 1)[0]
            self.assertIn("Public release verified", section)
            self.assertNotIn("not public", section)

    def test_v111_changelog_records_direct_start_port_support(self):
        changelog = (ROOT / "CHANGELOG.md").read_text(encoding="utf-8")
        self.assertIn("## [1.1.1] - 2026-08-26", changelog)
        self.assertIn("requested application port", changelog)


if __name__ == "__main__":
    unittest.main()
