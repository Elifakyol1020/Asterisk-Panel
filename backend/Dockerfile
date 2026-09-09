# Build with Java 21; no host JDK or Maven installation is needed.
FROM maven:3.9.16-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml ./
COPY src/main ./src/main
RUN mvn --batch-mode --no-transfer-progress -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app
COPY --from=build --chown=10001:10001 /build/target/asterisk-*.jar /app/asterisk.jar
USER 10001:10001
ENTRYPOINT ["java", "-jar", "/app/asterisk.jar"]
