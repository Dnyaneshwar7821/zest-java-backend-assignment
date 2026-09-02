# Zest India IT Assessment - Dockerfile
# Multi-stage Dockerfile for Spring Boot Application

# ==========================================
# Stage 1: Build & Package Artifact
# ==========================================
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /app

# Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package application
COPY src ./src
RUN mvn clean package -DskipTests -B

# ==========================================
# Stage 2: Minimal & Secure Runtime
# ==========================================
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

# Create a non-privileged dedicated user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Install curl for container health check
RUN apk add --no-cache curl

# Copy fat JAR from builder stage
COPY --from=builder /app/target/zest-backend-assignment-*.jar app.jar

# Set ownership
RUN chown -R appuser:appgroup /app

USER appuser:appgroup

# Expose API port
EXPOSE 8080

# Configure JVM memory and garbage collection options
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Container Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Dspring.profiles.active=prod -jar app.jar"]
