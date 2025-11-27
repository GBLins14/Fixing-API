FROM gradle:7.6.1-jdk17 AS builder
WORKDIR /app

ENV GRADLE_USER_HOME=/app/.gradle

COPY . .

RUN gradle --no-daemon clean build -x test

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
