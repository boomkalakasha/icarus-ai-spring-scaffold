# Icarus AI Spring Scaffold

![BOOMKALAKASHA 水印](docs/assets/brand/watermark-auto.svg)

[English](README.md) · [架构](docs/architecture.md) · [支持](SUPPORT.md)

> **从一句需求，生成一套可审查的服务骨架。**
>
> **From one idea to a reviewable service skeleton.**

从一句服务需求开始，Icarus 通过 CLI 或 REST 生成统一的 Java 17 / Spring Boot 多模块工程，让团队或协作 Agent 直接从可审查、可构建、可测试的起点继续开发。

`icarus-ai-spring-scaffold` 是一个安全优先、面向 AI 友好研发流程的
Spring Boot 项目生成器。它校验项目坐标，并在内存中生成 ZIP 后返回。生成的
项目是 Java 17 / Spring Boot 3 应用的可审查起点，不是托管服务，也不承诺任何
部署环境的可用性。

本仓库自身采用 [Apache License 2.0](LICENSE)，并遵循 SemVer。
<!-- icarus-release-fact: dynamic -->
公开物料请查看
[最新 GitHub Release](https://github.com/boomkalakasha/icarus-ai-spring-scaffold/releases/latest)
和[完整发布记录](https://github.com/boomkalakasha/icarus-ai-spring-scaffold/releases)。
远端发布门禁通过后，已审核标签会提供校验和、SBOM 与构建来源证明。

## 一眼看懂：它能帮你做什么

| 你的目标 | Icarus 提供什么 | 你会先得到什么 |
| --- | --- | --- |
| 开始一个 Java 服务 | 经过校验的 Spring Boot 多模块项目 ZIP | 不再从空目录起步，而是一套可审查骨架 |
| 让生成过程更安全 | 坐标约束、路径隔离、ZIP 穿越检查和禁止覆盖 | 更适合交给团队或 Agent 继续演进的可预测制品 |
| 选择接入方式 | 本地 CLI 与可选 REST 适配器 | 开发者、CI 或可信内部边缘都能调用的入口 |
| 确认生成项目是否能工作 | package、health、greeting，以及可选 Docker Compose 检查 | 针对样例的真实证据；不可用的检查会明确记为 `NOT_RUN` |

常见场景包括：快速启动一个小型 Spring 服务、为多个协作 Agent 提供一致的
项目起点，或在技术分享中演示服务结构。它是生成器和可审查的起点，不替代
具体业务设计、安全审查或部署运维。

## 60 秒快速开始

第一次使用不需要全局安装 Maven：直接使用仓库自带的 wrapper，并把生成的 ZIP
留在当前目录即可。

第一次使用按这四步走：

1. 先构建完整 reactor，让 CLI 及其依赖在本地可用。
2. 用下面的命令生成 `demo-service.zip`。
3. 解压后先阅读生成项目里的 `AGENTS.md`、README 和模块结构，再开始写业务代码。
4. 需要验证生成项目时，执行 `python scripts/generate-sample.py --root .`，获取
   package、运行态以及（Docker 可用时）Compose 证据。

CLI 默认仍将字节兼容的 ZIP 写到标准输出；想要更直观地得到文件时，使用
`--output` 写入当前目录的新文件。

按你的目标选择入口：

| 你想做什么 | 从哪里开始 |
| --- | --- |
| 本地或 CI 生成项目 | CLI 及其 `--output` 选项 |
| 让可信内部工具请求 ZIP | 可选 REST 适配器 |
| 验证完整样例链路 | `python scripts/generate-sample.py --root .` |

下面命令构建当前检出版本。使用公开物料时，请从最新 GitHub Release 开始，
并使用与该不可变标签一致的 jar 文件名。

POSIX shell：

```bash
./mvnw -B -ntp clean verify
java -jar icarus-scaffold-cli/target/icarus-scaffold-cli-1.1.4-all.jar \
  --artifact demo-service --group com.example.demo \
  --package com.example.demo --port 18080 \
  --description "Generated sample" --output demo-service.zip
```

PowerShell：

```powershell
.\mvnw.cmd -B -ntp clean verify
java -jar .\icarus-scaffold-cli\target\icarus-scaffold-cli-1.1.4-all.jar `
  --artifact demo-service --group com.example.demo `
  --package com.example.demo --port 18080 `
  --description "Generated sample" --output demo-service.zip
```

`--output` 只接受当前工作目录下的一个新 `*.zip` 文件名。绝对路径、嵌套路径、
`..`、非 ZIP 后缀和已存在目标都会被拒绝，并用 `CREATE_NEW` 防止校验与写入之间
的竞争覆盖。省略该选项即可保留标准输出契约。

## 你会得到什么

**示意生成结果——下面的目录和响应来自内置样例契约，不代表已有服务完成部署：**

```text
demo-service/
├── AGENTS.md
├── pom.xml
├── domain/
├── application/
├── infrastructure/
├── api/
└── boot/
```

```http
GET /api/greetings?subject=team
200 {"subject":"team","message":"Hello, team!"}

GET /actuator/health
200 {"status":"UP"}
```

默认 ZIP 不包含 `LICENSE`。只有实际权利人作出决定后，才同时提供
`--license`、`--copyright-holder` 和 `--copyright-year`；当前支持
`Apache-2.0` 与 `MIT`。

## 仓库内容

| 模块 | 职责 |
| --- | --- |
| `icarus-scaffold-core` | 输入校验、FreeMarker 渲染、安全路径和 ZIP 生成；不依赖 Web 或数据库。 |
| `icarus-scaffold-cli` | 本地命令行入口，写出生成 ZIP；不覆盖任意目录。 |
| `icarus-scaffold-server` | 可选 REST 适配器，返回 ZIP，设置请求限制和安全响应头，不接受服务器文件系统路径。 |
| `icarus-scaffold-core/.../templates/` | 生成的多模块项目及公开研发文档。 |

生成器核心与 HTTP、数据库边界分离。生成项目包含便于开始开发的应用分层和测试，
但投入生产前仍需独立完成依赖、安全、运维和许可证审查。

## 配套项目

- [AI-first Vibe Coding Skill](https://github.com/boomkalakasha/ai-first-vibe-coding-skill)：把生成的服务骨架交给边界清楚的 Agent 协作实现，再由独立 Reviewer 和主 Agent 用证据验收。
- [Icarus 开源治理](https://github.com/boomkalakasha/icarus-open-source-governance-skill)：准备公开发布时，检查来源、隐私、文档和发布证据。

## 环境要求

- JDK 17（Temurin 或其他兼容的 OpenJDK 发行版均可）。
- Maven 3.9+，或仓库提供的 Maven Wrapper。
- Python 3.10+，用于仓库验证脚本。
- Docker 与 Compose 仅在验证生成项目的容器文件时需要。

公开构建预期只从 Maven Central 解析依赖，不需要私有仓库、私有二进制或组织专属
服务。

## 构建与测试

```bash
./mvnw -B -ntp clean verify
python -m unittest discover -s scripts -p "test_*.py"
```

Windows PowerShell：

```powershell
.\mvnw.cmd -B -ntp clean verify
python -m unittest discover -s scripts -p "test_*.py"
```

默认 GitHub 工作流会构建完整 Maven reactor 并运行测试，然后生成样例、构建并测试
样例项目，最后扫描源代码和生成物中的公开内容风险。

## 本地生成 ZIP

CLI 默认将 ZIP 字节写入标准输出。需要输出到当前目录文件时，使用上面的安全
`--output demo-service.zip`；也可以在明确的外部策略下使用标准输出重定向：

```bash
java -jar icarus-scaffold-cli/target/icarus-scaffold-cli-1.1.4-all.jar \
  --artifact demo-service \
  --group com.example.demo \
  --package com.example.demo \
  --port 18080 \
  --description "Generated sample" > demo-service.zip
```

实际 jar 文件名包含项目版本；如果兼容版本调整了参数，请先执行 CLI 的 help
命令。CLI 不接受任意输出目录、覆盖开关、模板目录、shell 命令或服务器文件系统
路径。

执行跨平台端到端检查：

```bash
python scripts/generate-sample.py --root .
```

脚本会定位 CLI jar、校验 ZIP entry 路径、将样例解压到临时/验证目录，并在生成项目
中运行 Maven `package`，随后在有界时间内检查健康和 greeting 接口。只有 Docker 和
Compose 可用时才执行 Compose 解析、镜像构建和容器健康检查；否则报告明确记录
`NOT_RUN`。临时进程、容器、Compose 自建本地镜像和验证目录都会清理。如需适配兼容下游 CLI 参数，可设置
JSON 数组环境变量 `ICARUS_CLI_ARGS_JSON`；冷缓存 Docker 构建默认有 600 秒窗口，
也可以通过正数环境变量 `ICARUS_DOCKER_CHECK_TIMEOUT_SECONDS` 调整；ZIP 仍必须写入标准输出。

## 运行可选 REST 适配器

```bash
java -jar icarus-scaffold-server/target/icarus-scaffold-server-1.1.4.jar
curl --fail-with-body -H "Content-Type: application/json" \
  --data '{"artifact":"demo-service","group":"com.example.demo","package":"com.example.demo","port":18080,"description":"Demo service"}' \
  http://localhost:8080/api/scaffolds --output demo-service.zip
```

服务端只接受 JSON，拒绝未知字段，返回 `application/zip`，且永远不接受输出目录或覆盖指令。
它默认只绑定 `127.0.0.1`。如果确实需要暴露到网络，请设置 `SERVER_ADDRESS`，并在可信边缘补充
认证、限流、TLS 和运维监控；本仓库不是开箱即用的公网多租户服务。

## 安全边界

artifact、group、package、port、description 等输入有长度和白名单约束。可选的
许可证、权利人和年份必须作为完整声明一起提供；生成器会拒绝不完整声明，而不会
猜测归属。模板路径会规范化并限制在临时根目录内；ZIP entry 不允许绝对路径或父目录穿越。公开生成器
不接受 shell 命令、任意模板目录、覆盖开关，也不会生成默认凭据；不会完整记录生成
请求。

这些是可由源码和测试验证的边界。实际用于真实服务前，请审查生成项目的依赖和运行
时配置。

## 贡献与支持

提交 Pull Request 前请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。安全问题请按
[SECURITY.md](SECURITY.md) 私下报告，不要公开创建漏洞 issue。使用问题和可复现构建
问题请查看 [SUPPORT.md](SUPPORT.md)。

## 发布

发布 tag 使用 `vMAJOR.MINOR.PATCH`。发布自动化从精确 tag 构建，生成 SHA-256
校验和、CycloneDX SBOM，并在仓库设置允许时生成 GitHub 构建来源证明。这些材料
证明 tag 的构建过程，不证明生产部署、客户交付或流量切换。

离线发布说明见 [CHANGELOG.md](CHANGELOG.md)。

详细契约见 [CLI](docs/cli.md)、[REST API](docs/rest-api.md)、[生成项目](docs/generated-project.md)
和 [故障排查](docs/troubleshooting.md)。稳定的本地品牌副本位于
[docs/assets/brand](docs/assets/brand/)。
