# Icarus AI Spring Scaffold

[English](README.md) · [架构](docs/architecture.md) · [支持](SUPPORT.md)

`icarus-ai-spring-scaffold` 是一个安全优先、面向 AI 友好研发流程的
Spring Boot 项目生成器。它校验项目坐标，并在内存中生成 ZIP 后返回。生成的
项目是 Java 17 / Spring Boot 3 应用的可审查起点，不是托管服务，也不承诺任何
部署环境的可用性。

本仓库采用 [Apache License 2.0](LICENSE)，遵循 SemVer；已公开的首个稳定版本为
`v1.0.0`，这个 `v1.1.0` 功能分支是本地候选，只有在 PR、CI、不可变标签和 GitHub
Release 门禁被实际观察后才可称为公开发布。

## 60 秒快速开始

先构建完整 reactor，再生成一个 ZIP。CLI 默认仍将字节兼容的 ZIP 写到标准输出。

POSIX shell：

```bash
./mvnw -B -ntp clean verify
java -jar icarus-scaffold-cli/target/icarus-scaffold-cli-1.1.0-all.jar \
  --artifact demo-service --group com.example.demo \
  --package com.example.demo --port 18080 \
  --description "Generated sample" --output demo-service.zip
```

PowerShell：

```powershell
.\mvnw.cmd -B -ntp clean verify
java -jar .\icarus-scaffold-cli\target\icarus-scaffold-cli-1.1.0-all.jar `
  --artifact demo-service --group com.example.demo `
  --package com.example.demo --port 18080 `
  --description "Generated sample" --output demo-service.zip
```

`--output` 只接受当前工作目录下的一个新 `*.zip` 文件名。绝对路径、嵌套路径、
`..`、非 ZIP 后缀和已存在目标都会被拒绝，并用 `CREATE_NEW` 防止校验与写入之间
的竞争覆盖。省略该选项即可保留标准输出契约。

## 仓库内容

| 模块 | 职责 |
| --- | --- |
| `icarus-scaffold-core` | 输入校验、FreeMarker 渲染、安全路径和 ZIP 生成；不依赖 Web 或数据库。 |
| `icarus-scaffold-cli` | 本地命令行入口，写出生成 ZIP；不覆盖任意目录。 |
| `icarus-scaffold-server` | 可选 REST 适配器，返回 ZIP，设置请求限制和安全响应头，不接受服务器文件系统路径。 |
| `icarus-scaffold-core/.../templates/` | 生成的多模块项目及公开研发文档。 |

生成器核心与 HTTP、数据库边界分离。生成项目包含便于开始开发的应用分层和测试，
但投入生产前仍需独立完成依赖、安全、运维和许可证审查。

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
java -jar icarus-scaffold-cli/target/icarus-scaffold-cli-1.1.0-all.jar \
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
`NOT_RUN`。临时进程、容器和验证目录都会清理。如需适配兼容下游 CLI 参数，可设置
JSON 数组环境变量 `ICARUS_CLI_ARGS_JSON`；ZIP 仍必须写入标准输出。

## 运行可选 REST 适配器

```bash
java -jar icarus-scaffold-server/target/icarus-scaffold-server-1.1.0.jar
curl --fail-with-body -H "Content-Type: application/json" \
  --data '{"artifact":"demo-service","group":"com.example.demo","package":"com.example.demo","port":18080,"description":"Demo service"}' \
  http://localhost:8080/api/scaffolds --output demo-service.zip
```

服务端只接受 JSON，拒绝未知字段，返回 `application/zip`，且永远不接受输出目录或覆盖指令。
它默认只绑定 `127.0.0.1`。如果确实需要暴露到网络，请设置 `SERVER_ADDRESS`，并在可信边缘补充
认证、限流、TLS 和运维监控；本仓库不是开箱即用的公网多租户服务。

## 安全边界

artifact、group、package、port、description 等输入有长度和白名单约束。模板路径
会规范化并限制在临时根目录内；ZIP entry 不允许绝对路径或父目录穿越。公开生成器
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
