services:
  app:
    build: .
    ports:
      - "${port}:${port}"
    environment:
      SERVER_PORT: "${port}"
    healthcheck:
      test: ["CMD", "java", "-cp", "/app/healthcheck", "HealthCheck", "http://localhost:${port}/actuator/health"]
      interval: 30s
      timeout: 5s
      retries: 3
