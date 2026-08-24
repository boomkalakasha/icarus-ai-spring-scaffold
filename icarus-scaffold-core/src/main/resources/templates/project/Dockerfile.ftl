FROM maven:3.9-eclipse-temurin-17@sha256:bbcab0adfb03704e65831593df63b1c6b7ee42d5d8603f0d88972e0ee81a3036 AS build
WORKDIR /workspace
COPY pom.xml .
COPY domain/pom.xml domain/pom.xml
COPY application/pom.xml application/pom.xml
COPY infrastructure/pom.xml infrastructure/pom.xml
COPY api/pom.xml api/pom.xml
COPY boot/pom.xml boot/pom.xml
COPY domain/src domain/src
COPY application/src application/src
COPY infrastructure/src infrastructure/src
COPY api/src api/src
COPY boot/src boot/src
RUN mvn -B -ntp package -DskipTests

FROM eclipse-temurin:17-jre@sha256:13cc28a6cc72a38ce1f00c906be3580c1a3e604b8984d694f369a96742abc93b
WORKDIR /app
COPY --from=build /workspace/boot/target/boot-0.1.0-SNAPSHOT.jar app.jar
EXPOSE ${port}
ENTRYPOINT ["java", "-jar", "app.jar"]
