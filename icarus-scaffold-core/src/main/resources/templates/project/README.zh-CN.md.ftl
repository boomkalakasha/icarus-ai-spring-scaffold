# ${artifact}

${description}

[English](README.md) · [支持](SUPPORT.md) · [安全策略](SECURITY.md)

## 60 秒快速开始

```bash
mvn -B -ntp package
java -jar boot/target/boot-0.1.0-SNAPSHOT.jar
```

然后访问 `http://127.0.0.1:${port}/actuator/health` 和
`http://127.0.0.1:${port}/api/greetings?subject=team`。

这是一个 Java 17、Spring Boot 的五模块示例服务：

- `domain`：领域类型与不变量
- `application`：应用用例
- `infrastructure`：基础设施适配器
- `api`：HTTP 传输层
- `boot`：Spring Boot 启动与装配

## 本地运行

```text
mvn test
mvn spring-boot:run -pl boot
```

示例接口为 `GET /api/greetings?subject=team`，健康检查为
`GET /actuator/health`，默认端口为 `${port}`。

## 容器运行

```text
docker build -t ${artifact}:local .
docker compose up --build
```

镜像内置只依赖 JRE 的健康检查工具，Compose 不依赖 `curl`、`wget` 或额外
系统包。Compose 不包含有状态服务，也不要求凭据。发布或部署前请审阅生成的
源码和依赖清单。

## 支持与许可证

请阅读 [SUPPORT.md](SUPPORT.md)、[SECURITY.md](SECURITY.md) 和
[LICENSE](LICENSE)，为生成服务补充实际维护团队的支持、安全和许可证边界。
