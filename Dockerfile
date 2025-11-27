FROM gradle:7.6.1-jdk17 AS builder
WORKDIR /app

ENV GRADLE_OPTS="-Dorg.gradle.caching=false"
ENV GRADLE_USER_HOME=/app/.gradle

COPY . .

RUN gradle clean build -x test --no-daemon --no-build-cache

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
