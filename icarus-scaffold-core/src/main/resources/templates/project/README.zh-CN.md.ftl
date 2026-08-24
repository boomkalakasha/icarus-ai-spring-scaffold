# ${artifact}

${description}

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

Compose 不包含有状态服务，也不要求凭据。发布或部署前请审阅生成的
源码和依赖清单。
