# ${artifact}

${description}

[English](README.md) · [支持](SUPPORT.md) · [安全策略](SECURITY.md)

## 快速开始

这个 ZIP 使用 simple profile：一个 Maven 模块，内部按领域、应用、基础设施、
API 和启动包组织。它适合先跑通一个小服务；当这些层需要独立模块边界时，再选择
`modular` profile。

```bash
mvn -B -ntp package
java -jar target/${artifact}-0.1.0-SNAPSHOT.jar
```

然后访问 `http://127.0.0.1:${port}/actuator/health` 和
`http://127.0.0.1:${port}/api/greetings?subject=team`。

Scaffold 不解释自然语言需求，也不调用 AI 服务。当前 Vibe Coding 工具可以帮助把
对话整理为已确认的 CLI 参数，但选择 profile 的命令本身无需 AI 服务即可复现：

```text
icarus-scaffold --profile simple --artifact ${artifact} --group ${group} --package ${packageName} --port ${port}
```

## 本地运行与容器

```text
mvn test
mvn spring-boot:run
docker build -t ${artifact}:local .
docker compose up --build
```

示例接口为 `GET /api/greetings?subject=team`，健康检查为
`GET /actuator/health`，默认端口为 `${port}`。

镜像内置只依赖 JRE 的健康检查工具，Compose 不依赖 `curl`、`wget` 或额外系统包。
Compose 不包含有状态服务，也不要求凭据。发布或部署前请审阅生成的源码和依赖清单。

## 支持与许可证

请阅读 [SUPPORT.md](SUPPORT.md) 和 [SECURITY.md](SECURITY.md)，为生成服务补充
实际维护团队的支持与安全边界。
<#if licenseDeclared>
本项目在生成时显式选择了 `${license}` 许可证；[LICENSE](LICENSE) 中的权利人为
`${copyrightHolder}`，年份为 ${copyrightYear}。
<#else>
生成时没有声明项目许可证，因此 ZIP 会有意省略 `LICENSE`。公开分发前，应由实际
权利人决定并记录适用许可证。
</#if>
