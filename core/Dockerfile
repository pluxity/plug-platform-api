# Stage 1: Build the project
FROM eclipse-temurin:21-jdk-alpine as builder

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project to the container
COPY ../ .

# Grant execution permission to the Gradle wrapper
RUN chmod +x ./gradlew

# Build the core module (and its dependencies, including common)
RUN ./gradlew spotlessApply
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Runtime image
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Copy the built JAR file from the builder stage
COPY --from=builder /app/core/build/libs/*.jar app.jar

# Expose port 8080 (or adjust to your app's port)
EXPOSE 8080

# Set the command to run the application
CMD ["java", "-jar", "app.jar"]