# Icarus AI Spring Scaffold 开源提取设计

## 定位

`icarus-ai-spring-scaffold` 是一个安全优先、AI 友好的 Spring Boot 多模块项目生成器。它提取原内部脚手架的模块化、FreeMarker 生成、Docker 与 Agent 文档亮点，但不携带内部历史、公司标识、私有依赖、客户配置、商业二进制或 Jenkins 现场逻辑。

## 公开架构

- `icarus-scaffold-core`：输入校验、模板渲染、安全路径、ZIP 生成；无 Web/数据库依赖。
- `icarus-scaffold-cli`：本地命令行入口，只生成 ZIP，不覆盖任意目录。
- `icarus-scaffold-server`：可选 REST API，默认只返回 ZIP，设置请求大小和安全响应头；不提供服务器路径参数。
- `templates/`：生成一个 Java 17 / Spring Boot 3 的多模块服务，包含 `domain`、`application`、`infrastructure`、`api`、`boot`、测试、Docker、Compose、GitHub Actions、`AGENTS.md` 和双语友好说明。

## 安全边界

- artifact、group、package、端口和描述使用白名单/长度校验。
- 所有模板路径经过 normalize 后必须仍位于临时根目录；ZIP entry 禁止绝对路径和 `..`。
- 不接受 `outputPath`、`overwrite`、任意模板目录或 shell 命令。
- 不记录生成请求全文，不生成默认口令/JWT secret，不绑定内网地址。
- 公开历史从全新仓库开始；内部 Git 历史只作为本地来源审计，不同步。

## 依赖与许可证

- Java 17、Spring Boot 3.5.x、FreeMarker、Picocli、JUnit 5；均从 Maven Central 解析。
- 不包含私有组织依赖、商业 Office 组件、内部 Maven repository 或二进制扩展目录。
- 项目采用 Apache-2.0。发布者仍需确认 `Icarus` 名称不存在商标/内部代号冲突。

## 验收

1. reactor 单测和 package 通过。
2. CLI 与 REST 生成的 ZIP 不含路径穿越，且内容一致。
3. 解压生成样例后 `./mvnw test` 或系统 Maven `test` 通过。
4. 生成项目 Docker image 可构建，Compose 配置可解析（环境可用时）。
5. current tree 与打包物料的公司/内网/凭据/商业组件扫描无命中。
6. GitHub CI、CodeQL、Dependabot、PR/Issue 模板、Security、Release 流程完整。

## 与内部项目关系

这是安全提取的公开产品，不是内部仓库镜像，也不承诺与内部 Jenkins/客户部署 1:1 兼容。内部需求可以在私有 downstream 维护；可通用改进经公开 Issue/PR 回流。
