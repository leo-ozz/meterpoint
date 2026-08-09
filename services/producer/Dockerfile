# syntax=docker/dockerfile:1

# ---- Build stage ----
FROM eclipse-temurin:21-jdk-noble AS build
WORKDIR /build

# Wrapper and build definitions first, so dependency resolution
# is only re-run when these actually change.
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies

# Source last: editing a class invalidates only this layer onward.
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-noble AS runtime

RUN groupadd --system --gid 1001 app \
 && useradd --system --uid 1001 --gid app --no-create-home app

WORKDIR /app
COPY --from=build --chown=app:app /build/build/libs/*.jar app.jar

USER app
EXPOSE 8080

# Exec form: the JVM becomes PID 1 and receives SIGTERM directly.
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
