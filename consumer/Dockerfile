# Stage 1: Build
FROM eclipse-temurin:21-jdk AS builder
WORKDIR /build

# Copy Gradle wrapper and project configuration
COPY gradlew .
COPY gradlew.bat .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# Copy source code for all modules
COPY common common
COPY producer producer
COPY consumer consumer

# Build the specific module JAR
RUN ./gradlew :consumer:bootJar --no-daemon

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /build/consumer/build/libs/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
