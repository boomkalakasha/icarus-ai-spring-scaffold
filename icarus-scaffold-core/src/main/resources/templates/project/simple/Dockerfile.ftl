FROM maven:3.9-eclipse-temurin-17@sha256:bbcab0adfb03704e65831593df63b1c6b7ee42d5d8603f0d88972e0ee81a3036 AS build
WORKDIR /workspace
COPY pom.xml .
COPY src src
COPY docker/HealthCheck.java docker/HealthCheck.java
RUN mvn -B -ntp package -DskipTests
RUN javac -d /workspace/healthcheck docker/HealthCheck.java

FROM eclipse-temurin:17-jre@sha256:13cc28a6cc72a38ce1f00c906be3580c1a3e604b8984d694f369a96742abc93b
WORKDIR /app
COPY --from=build /workspace/target/${artifact}-0.1.0-SNAPSHOT.jar app.jar
COPY --from=build /workspace/healthcheck /app/healthcheck
EXPOSE ${port}
HEALTHCHECK --interval=10s --timeout=3s --start-period=20s --retries=6 CMD ["java", "-cp", "/app/healthcheck", "HealthCheck", "http://localhost:${port}/actuator/health"]
ENTRYPOINT ["java", "-jar", "app.jar"]
