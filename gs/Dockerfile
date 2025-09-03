# Stage 1: Build the project
FROM eclipse-temurin:21-jdk-alpine AS builder

RUN apk add --no-cache dos2unix

ARG WITH_PINPOINT=false

RUN mkdir -p /usr/local/pinpoint-agent-2.5.4
# Add Pinpoint
RUN if [ "$WITH_PINPOINT" = "true" ]; then \
      wget https://github.com/pinpoint-apm/pinpoint/releases/download/v2.5.4/pinpoint-agent-2.5.4.tar.gz -O /usr/local/pinpoint-agent-2.5.4.tar.gz && \
      tar -zxvf /usr/local/pinpoint-agent-2.5.4.tar.gz -C /usr/local && \
      sed -i 's/profiler.transport.grpc.collector.ip=127.0.0.1/profiler.transport.grpc.collector.ip=192.168.4.8/g' /usr/local/pinpoint-agent-2.5.4/pinpoint-root.config && \
      sed -i 's/profiler.collector.ip=127.0.0.1/profiler.collector.ip=192.168.4.8/g' /usr/local/pinpoint-agent-2.5.4/pinpoint-root.config && \
      sed -i 's#profiler.server.excludeurl=#profiler.server.excludeurl=/actuator,/actuator/**#g' /usr/local/pinpoint-agent-2.5.4/pinpoint-root.config ;\
    fi

# Set the working directory inside the container
WORKDIR /app

# Copy the entire project to the container
COPY ../ .

RUN dos2unix ./gradlew

# Grant execution permission to the Gradle wrapper
RUN chmod +x ./gradlew

# Build the core module (and its dependencies, including common)
RUN ./gradlew spotlessApply
RUN ./gradlew clean build -x test --no-daemon

# Stage 2: Runtime image
FROM eclipse-temurin:21-jdk-alpine

# Set the working directory inside the container
WORKDIR /app

# Pinpoint agent 복사
COPY --from=builder /usr/local/pinpoint-agent-2.5.4 /usr/local/pinpoint-agent-2.5.4

# Copy the built JAR file from the builder stage
COPY --from=builder /app/gs/build/libs/*.jar app.jar

# Expose port 8080 (or adjust to your app's port)
EXPOSE 8080

# Set the command to run the application
ENTRYPOINT ["sh", "-c", "JAVA_OPTS=''; if [ \"$WITH_PINPOINT\" = 'true' ]; then JAVA_OPTS=\"$JAVA_OPTS -javaagent:/usr/local/pinpoint-agent-2.5.4/pinpoint-bootstrap-2.5.4.jar -Dpinpoint.applicationName=plug_platform -Dpinpoint.config=/usr/local/pinpoint-agent-2.5.4/pinpoint-root.config\"; fi; exec java $JAVA_OPTS -jar app.jar"]