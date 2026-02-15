# Build stage
FROM gradle:8.10-jdk17-alpine AS builder

# Set working directory
WORKDIR /app

# Download dependencies
COPY gradle ./gradle
COPY settings.gradle.kts ./
COPY build.gradle.kts ./

RUN gradle dependencies --no-daemon

# Copy the source code
COPY src ./src

# Build the application
RUN gradle bootJar --no-daemon

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Set working directory
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
