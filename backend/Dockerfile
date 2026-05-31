# Runtime image — JAR is pre-built on host with: mvn clean package -DskipTests
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user for security
RUN addgroup -S appuser && adduser -S appuser -G appuser

# Copy pre-built JAR from host target directory
COPY target/secure-cloud-storage-*.jar app.jar

# Create upload directories
RUN mkdir -p /app/uploads /app/backups /app/logs && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
