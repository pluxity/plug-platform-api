# 1단계: 프로젝트 설정 및 아키텍처 (Setup & Architecture)

이 단계에서는 프로젝트를 로컬 환경에서 실행하고, 전체적인 모듈 구조를 파악하는 것을 목표로 합니다.

## 목표
- 프로젝트 빌드 및 실행 성공.
- 멀티 모듈 아키텍처(`common`, `core`, `collect`, `gs`) 이해.
- 간단한 API 생성을 통한 개발 흐름 파악.

## 진행 가이드

### 1. 프로젝트 클론 및 빌드
터미널에서 다음 명령어를 실행하여 의존성을 설치하고 빌드합니다.
```bash
./gradlew clean build -x test
```

### 2. 인프라 실행
Docker Compose를 사용하여 데이터베이스 등 필요한 인프라를 실행합니다.
```bash
docker-compose up -d
```

### 3. 모듈 구조 분석
프로젝트는 다음과 같은 주요 모듈로 구성되어 있습니다. 각 모듈의 `build.gradle.kts`와 패키지 구조를 살펴보세요.

- **common**: 모든 모듈에서 공통으로 사용하는 엔티티(`User`, `Device` 등), 유틸리티, 설정 등을 포함합니다.
    - 주요 경로: `common/src/main/kotlin/com/pluxity`
- **core**: 핵심 비즈니스 로직과 API 서버 기능을 담당합니다.
    - 주요 경로: `core/src/main/kotlin/com/pluxity`
- **collect**: 데이터 수집과 관련된 로직을 처리합니다.
- **gs**: (Global Service 또는 특정 서비스) 관련 로직을 담당합니다.

### 4. 실습 과제: Hello World API 만들기
`core` 모듈에서 간단한 API를 만들어보며 개발 환경이 정상적인지 확인합니다.

1.  **브랜치 생성**: `onboarding/step-1-setup` 브랜치를 생성하고 체크아웃하세요.
2.  **Controller 생성**: `core` 모듈의 적절한 위치(예: `com.pluxity.global.controller`)에 `HelloController`를 생성합니다.
3.  **Endpoint 구현**: `GET /api/v1/hello` 요청 시 "Hello, Onboarding!"을 반환하는 메소드를 작성하세요.
4.  **테스트**: 애플리케이션(`CoreApplication.kt`)을 실행하고, 브라우저나 Postman으로 호출하여 응답을 확인하세요.

## 완료 조건
- [ ] `./gradlew build`가 에러 없이 성공한다.
- [ ] Docker 컨테이너들이 정상적으로 실행 중이다.
- [ ] `GET /api/v1/hello` 호출 시 정상 응답을 받는다.

다음 단계로 넘어가기 전에 변경 사항을 커밋하고 푸시하세요.
