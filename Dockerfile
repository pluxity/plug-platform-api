# Build Stage
FROM gradle:8.4.0-jdk21 AS builder

# 작업 디렉토리 생성
WORKDIR /app

# Gradle 캐싱을 위해 build.gradle 파일과 설정 파일 복사
COPY settings.gradle build.gradle ./
COPY project-a/build.gradle ./project-a/

# 의존성 캐싱
RUN gradle :project-a:dependencies --no-daemon || return 0

# 소스 코드 복사 및 빌드
COPY project-a/src ./project-a/src
RUN gradle :project-a:spotlessApply --no-daemon
RUN gradle :project-a:build -x test --no-daemon #-Pprofile=stage

# Runtime Stage
FROM eclipse-temurin:21-jre

# 작업 디렉토리 생성
WORKDIR /app

# 빌드 결과물 복사
COPY --from=builder /app/project-a/build/libs/*.jar plug-platform-api.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "plug-platform-api.jar"]

# 컨테이너 노출 포트
EXPOSE 18080