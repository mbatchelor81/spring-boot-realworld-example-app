# ---------------------------------------------------------------------------
# Multi-stage Dockerfile — ftgo-api-gateway
# ---------------------------------------------------------------------------
# Builder: Gradle with dependency caching
# Runtime: Minimal JRE 17 with non-root user and health check
# ---------------------------------------------------------------------------

# ---------------------------------------------------------------------------
# Stage 1 — Build
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /workspace

# Copy Gradle wrapper and make executable
COPY gradlew gradlew
COPY gradle/wrapper/ gradle/wrapper/
RUN chmod +x gradlew

# Copy build configuration (cached unless these files change)
COPY settings.gradle settings.gradle
COPY build.gradle build.gradle
COPY gradle/plugins/ gradle/plugins/
COPY gradle/libs.versions.toml gradle/libs.versions.toml

# Copy all service build files and sources
COPY services/ services/

# Build the service JAR (tests run separately in CI)
RUN ./gradlew :services:ftgo-api-gateway:bootJar \
    -x test -x spotlessCheck -x jacocoTestCoverageVerification \
    --no-daemon

# ---------------------------------------------------------------------------
# Stage 2 — Runtime
# ---------------------------------------------------------------------------
FROM eclipse-temurin:17-jre-alpine

LABEL org.opencontainers.image.source="https://github.com/mbatchelor81/spring-boot-realworld-example-app"
LABEL org.opencontainers.image.description="FTGO API Gateway"

RUN addgroup -S ftgo && adduser -S ftgo -G ftgo

WORKDIR /app

COPY --chown=ftgo:ftgo --from=builder /workspace/services/ftgo-api-gateway/build/libs/*.jar app.jar

USER ftgo

EXPOSE 8085

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:8085/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
