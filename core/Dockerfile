# Build stage
FROM gradle:8.6.0-jdk21 AS build
WORKDIR /app

# Copy the entire project
COPY . .

# Build the application
RUN gradle :core:bootJar --no-daemon

# Run stage
FROM openjdk:21-slim
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/core/build/libs/*.jar app.jar

# Create upload directory
RUN mkdir -p /app/upload

# Create a non-root user
RUN useradd -m -u 1000 appuser && \
    chown -R appuser:appuser /app
USER appuser

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application
CMD ["java", "-jar", "app.jar"] 