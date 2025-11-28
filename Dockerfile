FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY . .

RUN dos2unix gradlew || true
RUN chmod 755 gradlew

ENV GRADLE_USER_HOME=/app/.gradle
ENV GRADLE_OPTS="-Dorg.gradle.caching=false"

RUN ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
