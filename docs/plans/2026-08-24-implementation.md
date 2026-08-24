# Icarus AI Spring Scaffold Implementation Plan
> **For agentic workers:** Execute this plan task-by-task. Use isolated subagents with review checkpoints when the host supports them.

**Goal:** 从内部脚手架的已验证亮点提取一个无私有依赖、默认安全、可由 GitHub CI 构建并发布的 Spring Boot 项目生成器。

**Architecture:** 核心生成器、CLI、可选 REST server 分离；生成器只输出内存 ZIP；模板生成可独立编译的多模块项目；公开治理、验证和发版均由 GitHub 原生文件表达。

**Tech Stack:** Java 17, Maven, Spring Boot 3.5.x, FreeMarker, Picocli, JUnit 5, Docker, GitHub Actions.

---

1. 先建立无私有 repository 的 root reactor 与模块边界。
2. 先写 artifact/group/package/port/path/ZIP 安全负向测试，再实现校验和渲染。
3. 实现 CLI 与默认 ZIP-only REST API，补契约和 MockMvc 测试。
4. 建立最小但完整的生成项目模板和 AI 研发文档。
5. 验证生成样例 Maven 构建、启动 health、Docker/Compose（能力可用时）。
6. 补齐双语 README、AGENTS、贡献、安全、支持、许可证、变更日志和架构文档。
7. 添加 fork-safe CI、CodeQL、Dependabot、SBOM/校验和与 tag Release。
8. 执行 current tree/history/产物扫描和独立代码审查，P0/P1 清零后提交。
9. 创建公开 GitHub 仓库，推送初始 `main`，设置 `v1.0.0` 并核验 Actions/Release；仓库保护设置单独核验。
