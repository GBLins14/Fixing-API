FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY gradlew .
COPY gradle gradle
RUN chmod +x gradlew

COPY . .

ENV GRADLE_OPTS="-Dorg.gradle.caching=false"
ENV GRADLE_USER_HOME=/app/.gradle

RUN ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
