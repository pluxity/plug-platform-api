FROM gradle:8.6.0-jdk21 AS build
WORKDIR /app

COPY . .

RUN gradle :core:bootJar --no-daemon

FROM openjdk:21-slim
WORKDIR /app

COPY --from=build /app/core/build/libs/*.jar plug-platform.jar

RUN mkdir -p /app/upload

RUN useradd -m -u 1000 pluxity && \
    chown -R pluxity:pluxity /app
USER pluxity

EXPOSE 8080

CMD ["java", "-jar", "plug-platform.jar"]