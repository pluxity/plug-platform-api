# Build stage
FROM gradle:8.6.0-jdk21 AS build
WORKDIR /app

# Copy the entire project
COPY . .

# Build the application with profile
ARG SPRING_PROFILES_ACTIVE=prod
ENV SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE}

# Build the application
RUN gradle :core:bootJar --no-daemon

# Run stage
FROM openjdk:21-slim
WORKDIR /app

# Copy the built jar from build stage
COPY --from=build /app/core/build/libs/*.jar app.jar

# Create a non-root user
RUN useradd -m -u 1000 appuser && \
    chown -R appuser:appuser /app
USER appuser

# Expose the port the app runs on
EXPOSE 8080

# Command to run the application with profile
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}"] 